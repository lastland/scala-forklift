create table "user_emails" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "user_id" INTEGER NOT NULL,
  "email" VARCHAR NOT NULL
);
ALTER TABLE "user_emails"
  ADD FOREIGN KEY ("user_id")
  REFERENCES "users"("id");
