-- Add end_time column to sessions table
-- Tracks when a live class session ends
ALTER TABLE sessions ADD COLUMN IF NOT EXISTS end_time TIME;
