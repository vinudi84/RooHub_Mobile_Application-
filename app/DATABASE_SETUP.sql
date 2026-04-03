/**
 * ============================================================================
 * DATABASE CONFIGURATION - Supabase Setup for Course Upload Feature
 * ============================================================================
 * 
 * This file documents the required SQL schema for the course upload feature.
 * Execute this SQL in your Supabase Database Editor to set up the tables.
 * 
 * Navigate to: Supabase Console > Database > SQL Editor > Create a new query
 * Copy and paste the following SQL and execute it.
 * 
 * ============================================================================
 */

-- Create course_uploads table for storing course submissions with VIDEO support
CREATE TABLE public.course_uploads (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    course_name VARCHAR(255) NOT NULL,
    course_category VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    video_url TEXT NOT NULL,
    created_at BIGINT,
    updated_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'active',
    views INT DEFAULT 0
);

-- Create index on user_id for faster queries
CREATE INDEX idx_course_uploads_user_id ON public.course_uploads(user_id);

-- Create index on course_category for filtering
CREATE INDEX idx_course_uploads_category ON public.course_uploads(course_category);

-- Create index on created_at for sorting
CREATE INDEX idx_course_uploads_created_at ON public.course_uploads(created_at DESC);

-- Enable Row Level Security (RLS) on course_uploads table
ALTER TABLE public.course_uploads ENABLE ROW LEVEL SECURITY;

-- Create policy: Users can view all courses
CREATE POLICY "Enable read access for all users" 
ON public.course_uploads 
FOR SELECT 
USING (true);

-- Create policy: Users can only insert their own courses
CREATE POLICY "Enable insert for authenticated users"
ON public.course_uploads
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Create policy: Users can only update their own courses
CREATE POLICY "Enable update for course owners"
ON public.course_uploads
FOR UPDATE
USING (auth.uid() = user_id);

-- Create policy: Users can only delete their own courses
CREATE POLICY "Enable delete for course owners"
ON public.course_uploads
FOR DELETE
USING (auth.uid() = user_id);

-- ============================================================================
-- STORAGE BUCKET SETUP - Required Buckets
-- ============================================================================
/*
Create the following buckets in Supabase Storage:

1. COURSE-VIDEOS (NEW - for course content videos)
   - Name: "course-videos"
   - Public bucket: Yes
   - File size limit: 500MB or more (for video files)

2. ART-IMAGES (existing - for artwork images)
3. PROFILE-IMAGES (existing - for profile pictures)

Steps to create bucket:
1. Navigate to Supabase Console > Storage
2. Click "Create a new bucket"
3. Enter the bucket name
4. Set public/private as needed
5. Set file size limit
6. Click Create

After creating buckets, configure policies as shown below:
*/

-- Storage policies for course-videos bucket
CREATE POLICY "Enable upload for authenticated users"
ON storage.objects
FOR INSERT
WITH CHECK (
    bucket_id = 'course-videos' 
    AND auth.role() = 'authenticated'
);

CREATE POLICY "Enable read access for all"
ON storage.objects
FOR SELECT
USING (bucket_id = 'course-videos');

-- ============================================================================
-- TABLE STRUCTURE REFERENCE - course_uploads
-- ============================================================================
/*
Columns:
- id (BIGSERIAL): Primary key, auto-incremented
- user_id (UUID): Foreign key to auth.users, identifies course owner
- course_name (VARCHAR): Name of the course
- course_category (VARCHAR): Type of course (Pencil Art, Coloring, Assemblage Art)
- description (TEXT): Detailed description of the course
- video_url (TEXT): URL to the course video stored in Supabase Storage
- created_at (BIGINT): Timestamp when course was created (milliseconds)
- updated_at (TIMESTAMP): Last update timestamp
- status (VARCHAR): Status (active, inactive, pending, archived, etc.)
- views (INT): Number of times the course has been viewed

Sample queries:
-- Get courses for a specific user
SELECT * FROM course_uploads 
WHERE user_id = 'USER_UUID' 
ORDER BY created_at DESC;

-- Get all courses by category
SELECT * FROM course_uploads 
WHERE course_category = 'Pencil Art' AND status = 'active' 
ORDER BY created_at DESC;

-- Get top viewed courses
SELECT * FROM course_uploads 
WHERE status = 'active' 
ORDER BY views DESC 
LIMIT 10;

-- Get recent courses (last 30 days)
SELECT * FROM course_uploads 
WHERE created_at > (EXTRACT(EPOCH FROM NOW()) - 30*24*60*60)*1000 
ORDER BY created_at DESC;
*/

-- ============================================================================
-- SUPPORTED VIDEO FORMATS
-- ============================================================================
/*
The application supports the following video formats:
- MP4 (.mp4)     - Most common, widely supported
- MOV (.mov)     - QuickTime format
- AVI (.avi)     - Windows standard
- MKV (.mkv)     - Matroska format
- WebM (.webm)   - Web video format
- 3GP (.3gp)     - Mobile video format

Recommended format: MP4 (H.264 codec, AAC audio) for best compatibility
Maximum file size: Adjustable in bucket settings (default 500MB recommended)
*/

-- ============================================================================
-- ANALYTICS TRACKING (Optional Enhancement)
-- ============================================================================
/*
To track course views, you can update the views count:

CREATE OR REPLACE FUNCTION increment_course_views(course_id BIGINT)
RETURNS void AS $$
BEGIN
    UPDATE public.course_uploads
    SET views = views + 1
    WHERE id = course_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

Then call it from your app when a course video is played.
*/

-- ============================================================================
-- MIGRATION GUIDE - If upgrading from image to video schema
-- ============================================================================
/*
If you previously had image_url column, migrate it to video_url:

-- Add video_url column
ALTER TABLE public.course_uploads 
ADD COLUMN video_url TEXT;

-- Optionally copy image URLs as video URLs (if they were videos before)
UPDATE public.course_uploads 
SET video_url = image_url 
WHERE image_url IS NOT NULL;

-- Drop old column (only if you don't need it anymore)
ALTER TABLE public.course_uploads 
DROP COLUMN image_url;
*/

-- ============================================================================
-- CLEANUP/MAINTENANCE QUERIES
-- ============================================================================
/*
-- Delete all courses for testing
DELETE FROM public.course_uploads;

-- Delete courses older than 90 days
DELETE FROM public.course_uploads 
WHERE created_at < (EXTRACT(EPOCH FROM NOW()) - 90*24*60*60)*1000;

-- Reset view counts
UPDATE public.course_uploads SET views = 0;

-- Get statistics
SELECT 
    course_category, 
    COUNT(*) as total_courses,
    AVG(views) as avg_views,
    MAX(views) as max_views
FROM public.course_uploads
WHERE status = 'active'
GROUP BY course_category;
*/

-- ============================================================================
-- BACKUP - Full Restore from CSV
-- ============================================================================
/*
To backup your data:
1. Go to Supabase > Database > course_uploads
2. Click more options (...) > Download as CSV

To restore:
1. Go to Supabase > SQL Editor
2. Use COPY command to import CSV data
*/
