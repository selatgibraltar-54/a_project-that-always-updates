const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_user_123";
const BOB_UID = "bob_user_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

// --- USER PROFILE RULES TESTS ---

test("Unauthenticated user: cannot create user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      wins: 0,
      losses: 0,
      level: 1,
      deviceId: "dev_alice_phone",
      isOnline: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create their own profile with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice In Wonderland",
      wins: 5,
      losses: 2,
      level: 3,
      deviceId: "dev_pixel_9_pro",
      isOnline: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create profile for another user ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Spoofer",
      wins: 0,
      losses: 0,
      level: 1,
      deviceId: "dev_bob_phone",
      isOnline: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot update immutable createdAt or userId", async () => {
  const createdDate = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      wins: 0,
      losses: 0,
      level: 1,
      deviceId: "dev_alice_phone",
      isOnline: true,
      createdAt: createdDate,
      updatedAt: createdDate,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Changing userId or createdAt must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      userId: "new_hacked_id",
      updatedAt: new Date(),
    })
  );

  // Updating allowed fields succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Master",
      wins: 1,
      updatedAt: new Date(),
    })
  );
});

// --- MATCH ROOM RULES TESTS ---

test("Authenticated user: can create a match room as host", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1001").set({
      matchId: "match_1001",
      hostUserId: ALICE_UID,
      hostDisplayName: "Alice",
      hostDeviceId: "dev_alice_phone",
      status: "waiting",
      currentTurnUserId: ALICE_UID,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create match with another user as host", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("matches").doc("match_1002").set({
      matchId: "match_1002",
      hostUserId: BOB_UID,
      hostDisplayName: "Bob",
      hostDeviceId: "dev_bob_phone",
      status: "waiting",
      currentTurnUserId: BOB_UID,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
