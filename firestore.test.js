const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validEntry(uid, overrides = {}) {
  return {
    userId: uid,
    type: "in",
    amount: 1500.5,
    dateStr: "2026-11-15",
    description: "পণ্য বিক্রি",
    clientOrder: 1731680000000,
    createdAt: new Date(),
    updatedAt: new Date(),
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write entries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/entries`).get());
  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/entries`).doc("entry_1").set(validEntry(ALICE_UID))
  );
});

test("Authenticated user can create, list, update, and delete own entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection(`users/${ALICE_UID}/entries`).doc("entry_1");

  await assertSucceeds(docRef.set(validEntry(ALICE_UID)));
  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/entries`).get());
  await assertSucceeds(
    docRef.update({
      type: "out",
      amount: 750,
      dateStr: "2026-11-16",
      description: "দোকান ভাড়া",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("Cross-user isolation: Bob cannot read, create, update, or delete Alice's entries", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection(`users/${ALICE_UID}/entries`)
      .doc("entry_alice")
      .set(validEntry(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection(`users/${ALICE_UID}/entries`).get());
  await assertFails(bobDb.collection(`users/${ALICE_UID}/entries`).doc("entry_alice").get());
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/entries`).doc("entry_bob").set(validEntry(ALICE_UID))
  );
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/entries`).doc("entry_alice").update({
      amount: 999,
      updatedAt: new Date(),
    })
  );
  await assertFails(bobDb.collection(`users/${ALICE_UID}/entries`).doc("entry_alice").delete());
});

test("Validation rejects shadow fields, negative amounts, bad dates, and immutable field mutation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const colRef = aliceDb.collection(`users/${ALICE_UID}/entries`);

  // Ghost / shadow field
  await assertFails(colRef.doc("e_ghost").set(validEntry(ALICE_UID, { isAdmin: true })));
  // Non-positive amount
  await assertFails(colRef.doc("e_zero").set(validEntry(ALICE_UID, { amount: 0 })));
  await assertFails(colRef.doc("e_neg").set(validEntry(ALICE_UID, { amount: -10 })));
  // Invalid type
  await assertFails(colRef.doc("e_type").set(validEntry(ALICE_UID, { type: "transfer" })));
  // Invalid date format
  await assertFails(colRef.doc("e_date").set(validEntry(ALICE_UID, { dateStr: "15-11-2026" })));
  // Invalid document ID
  await assertFails(colRef.doc("bad id with spaces").set(validEntry(ALICE_UID)));

  // Create valid entry then try mutating immutable clientOrder
  await assertSucceeds(colRef.doc("e_imm").set(validEntry(ALICE_UID)));
  await assertFails(
    colRef.doc("e_imm").update({
      clientOrder: 999,
      updatedAt: new Date(),
    })
  );
});
