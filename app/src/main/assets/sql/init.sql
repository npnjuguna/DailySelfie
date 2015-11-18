--
-- Table structure for table selfies
--

DROP TABLE IF EXISTS selfies;
CREATE TABLE selfies (
  _id INTEGER primary key autoincrement,
  guid TEXT NOT NULL,
  cDate integer,
  cBy TEXT NOT NULL,
  cLat TEXT,
  cLng TEXT,
  uDate integer,
  uBy TEXT,
  uLat TEXT,
  uLng TEXT,
  caption TEXT,
  original_path TEXT,
  thumbnail_path TEXT,
  saved_path TEXT,
  saved_image BLOB
);

DROP INDEX IF EXISTS selfies_guid_ndx; CREATE UNIQUE INDEX IF NOT EXISTS selfies_guid_ndx ON selfies (guid);
DROP INDEX IF EXISTS selfies_caption_ndx; CREATE INDEX IF NOT EXISTS selfies_caption_ndx ON selfies (caption);
DROP INDEX IF EXISTS selfies_cDate_ndx; CREATE INDEX IF NOT EXISTS selfies_cDate_ndx ON selfies (cDate);
DROP INDEX IF EXISTS selfies_uDate_ndx; CREATE INDEX IF NOT EXISTS selfies_uDate_ndx ON selfies (uDate);
DROP INDEX IF EXISTS selfies_cBy_ndx; CREATE INDEX IF NOT EXISTS selfies_cBy_ndx ON selfies (cBy);

--
-- Table structure for table selfies_fts
--

DROP TABLE IF EXISTS selfies_fts;
CREATE TABLE selfies_fts (
  _id,
  guid,
  caption,
  original_path,
  thumbnail_path
);

--
-- Table structure for table reminders
--

DROP TABLE IF EXISTS reminders;
CREATE TABLE reminders (
  _id INTEGER primary key autoincrement,
  guid TEXT NOT NULL,
  cDate integer,
  cBy TEXT,
  cLat TEXT,
  cLng TEXT,
  uDate integer,
  uBy TEXT,
  uLat TEXT,
  uLng TEXT,
  init_time integer
);

DROP INDEX IF EXISTS reminders_guid_ndx; CREATE UNIQUE INDEX IF NOT EXISTS reminders_guid_ndx ON reminders (guid);
DROP INDEX IF EXISTS reminders_cBy_ndx; CREATE INDEX IF NOT EXISTS reminders_cBy_ndx ON reminders (cBy);

