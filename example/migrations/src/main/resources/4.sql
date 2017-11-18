create table "user_email" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "user_id" INTEGER NOT NULL,
  "email" VARCHAR NOT NULL
);
ALTER TABLE "user_email"
  ADD FOREIGN KEY ("user_id")
  REFERENCES "user"("id");
