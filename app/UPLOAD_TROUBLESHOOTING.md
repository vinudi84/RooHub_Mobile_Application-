# Course Upload Troubleshooting Guide

## Common Upload Errors & Solutions

---

## ❌ Error 1: "Session error: User ID missing" or "Auth token missing"

**What's happening:** The app lost the user session or authentication failed.

**Solutions:**
1. **Log out and log back in**
   - Go to settings/menu
   - Select Logout
   - Log in again

2. **Clear app data:**
   - Settings > Apps > RooHub > Clear Data
   - Log in again

3. **Check SessionManager:**
   - Make sure auth token is being saved properly
   - Verify on backend that user is logged in

---

## ❌ Error 2: "Please select a course category"

**What's happening:** User didn't select a course category from the dropdown.

**Solutions:**
1. Click the "Course Category" dropdown
2. Select one of these options:
   - Pencil Art
   - Coloring
   - Assemblage Art
3. Try uploading again

---

## ❌ Error 3: "Video file is too large (max 500MB)"

**What's happening:** Selected video exceeds the 500MB file size limit.

**Solutions:**
1. **Compress the video:**
   - Use video editing software (HandBrake, FFmpeg)
   - Reduce resolution to 720p or 1080p
   - Reduce bitrate to 5-8 Mbps
   - Use MP4 format

   **FFmpeg example:**
   ```bash
   ffmpeg -i input.mp4 -vf scale=1280:-1 -b:v 5M output.mp4
   ```

2. **Use smaller video clips**
   - Split long videos into parts
   - Upload each part separately

3. **Recommended video settings:**
   - **Format:** MP4 (H.264)
   - **Resolution:** 720p - 1080p
   - **Bitrate:** 5-8 Mbps
   - **Audio:** AAC, 128 kbps
   - **Duration:** 5-30 minutes per video
   - **Max file size:** Under 300MB

---

## ❌ Error 4: "Upload error: Image upload failed (403)"

**What's happening:** Permission denied - can't upload to storage bucket.

**Solutions:**

1. **Verify bucket exists in Supabase:**
   - Go to Supabase Console > Storage
   - Check if "course-videos" bucket exists
   - If not, create it:
     - Click "Create new bucket"
     - Name: `course-videos`
     - Public: Yes
     - Size limit: 500MB or more

2. **Check bucket policies:**
   ```sql
   CREATE POLICY "Enable upload for authenticated users"
   ON storage.objects
   FOR INSERT
   WITH CHECK (
       bucket_id = 'course-videos' 
       AND auth.role() = 'authenticated'
   );
   ```

3. **Verify RLS is enabled:**
   - Go to Storage > course-videos > Policies
   - Make sure INSERT policy exists for authenticated users

---

## ❌ Error 5: "Upload error: Cannot read video file"

**What's happening:** App can't access the selected video file.

**Solutions:**

1. **Check file permissions:**
   - Settings > Apps > RooHub > Permissions
   - Enable "Files and media" or "Read external storage"
   - Re-run the app

2. **Try different video:**
   - Select a different video file
   - Try from different storage location (Downloads, Pictures)

3. **For Android 11+:**
   - Make sure app has `READ_MEDIA_VIDEO` permission
   - Check AndroidManifest.xml for permission

---

## ❌ Error 6: "Upload error: Video upload failed (401)"

**What's happening:** Authentication token is invalid or expired.

**Solutions:**

1. **Re-authenticate:**
   - Log out: Menu > Logout
   - Log back in
   - Try upload again

2. **Check token expiration:**
   - Tokens expire after some time
   - New login generates new token
   - Implement token refresh in backend

3. **Verify Supabase credentials:**
   - Check SupabaseClient.kt has correct SUPABASE_URL
   - Check SUPABASE_ANON_KEY matches project

---

## ❌ Error 7: "Database submission error"

**What's happening:** Video uploaded successfully but database submission failed.

**Solutions:**

1. **Verify table exists:**
   ```sql
   SELECT * FROM course_uploads LIMIT 1;
   ```

2. **Check table columns:**
   - user_id (UUID)
   - course_name (VARCHAR)
   - course_category (VARCHAR)
   - description (TEXT)
   - video_url (TEXT)
   - created_at (BIGINT)

3. **Verify RLS policies on course_uploads:**
   ```sql
   ALTER TABLE public.course_uploads ENABLE ROW LEVEL SECURITY;
   
   CREATE POLICY "Enable insert for authenticated users"
   ON public.course_uploads
   FOR INSERT
   WITH CHECK (auth.uid() = user_id);
   ```

4. **Check API endpoint:**
   - Verify REST API is enabled in Supabase
   - Check PostgREST configuration

---

## ❌ Error 8: "Network timeout"

**What's happening:** Network connection is slow or unstable.

**Solutions:**

1. **Check internet connection:**
   - Make sure WiFi or mobile data is working
   - Try downloading something to verify speed

2. **Use better network:**
   - Switch from mobile data to WiFi
   - Move closer to WiFi router
   - Try at different location

3. **Reduce video size:**
   - Smaller files upload faster
   - Use lower resolution or bitrate

4. **Check Supabase status:**
   - Visit https://status.supabase.com
   - Verify no ongoing incidents

---

## ❌ Error 9: "Video file is empty"

**What's happening:** Selected file has 0 bytes size.

**Solutions:**

1. **Video file corrupted:**
   - Re-download or record video again
   - Use different video player to verify it plays

2. **Wrong file selected:**
   - Make sure you selected a video, not an image
   - Check file has .mp4, .mov, .avi extension

3. **File location issue:**
   - Try selecting from different folder
   - Try copying video to Pictures folder first

---

## ✅ How to Debug Upload Errors

### View Logs in Android Studio:

1. **Open Logcat:**
   - Android Studio > Logcat tab (bottom)
   
2. **Filter for CourseFormActivity:**
   ```
   D: CourseFormActivity - Form submission started
   D: CourseFormActivity - Video file size: X MB
   D: CourseFormActivity - Uploading video to: https://...
   D: CourseFormActivity - Storage response code: 200
   D: CourseFormActivity - Submitting form to database
   ```

3. **Look for error tags:**
   - Search: "Upload error"
   - Search: "E/CourseFormActivity"
   - Search: "Validation failed"

---

## 🔍 Step-by-Step Debug Process

1. **Fill form and try to submit**
2. **Watch Logcat for errors**
3. **Note the exact error message**
4. **Check corresponding solution above**
5. **If still failing, verify:**
   - Supabase credentials
   - Network connection
   - File size limits
   - Bucket existence
   - RLS policies

---

## 📋 Pre-Upload Checklist

Before uploading, verify:

- [ ] User is logged in (Session valid)
- [ ] Course name is 3+ characters
- [ ] Description is 10+ characters
- [ ] Course category is selected (not default)
- [ ] Video file is selected
- [ ] Video file is less than 500MB
- [ ] Video file format is supported (MP4, MOV, AVI, MKV, WebM, 3GP)
- [ ] Internet connection is stable
- [ ] `course-videos` bucket exists in Supabase
- [ ] RLS policies are configured
- [ ] Auth token is valid

---

## 🔧 Supabase Configuration Verification

Run these SQL queries to verify setup:

```sql
-- Check course_uploads table exists
SELECT * FROM information_schema.tables 
WHERE table_name = 'course_uploads';

-- Check RLS is enabled
SELECT * FROM pg_tables 
WHERE tablename = 'course_uploads' 
AND rowsecurity = true;

-- List all policies on course_uploads
SELECT * FROM pg_policies 
WHERE tablename = 'course_uploads';

-- Get storage buckets
SELECT * FROM storage.buckets 
WHERE name = 'course-videos';
```

---

## 🛠️ Server-Side Logging

Add logging to your backend to capture upload requests:

```sql
-- Example: Log all uploads
CREATE TABLE upload_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID,
    course_name VARCHAR,
    status VARCHAR,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insert logs after upload attempts
INSERT INTO upload_logs (user_id, course_name, status, error_message)
VALUES ($1, $2, 'failed', $3);
```

---

## 🚀 Performance Tips

1. **Compress videos before uploading:**
   - Reduces upload time
   - Uses less storage
   - Better for viewing on mobile

2. **Use WiFi for uploads:**
   - Faster and more reliable
   - Doesn't use mobile data

3. **Upload during off-peak hours:**
   - Less network congestion
   - Faster speeds

4. **Test with small file first:**
   - Upload small test video
   - Verify process works
   - Then upload larger videos

---

## 📞 Still Having Issues?

If none of these steps work:

1. **Check Logcat for full error trace**
2. **Verify all Supabase configuration**
3. **Test network with other apps**
4. **Try uploading from different device**
5. **Review REST API response in network monitor**

---

## Log Messages to Look For

| Message | Meaning | Action |
|---------|---------|--------|
| "Session validated" | ✅ Auth OK | Continue |
| "Video file size: X MB" | ✅ File readable | Continue |
| "Step 1: Uploading" | 🔄 Starting upload | Wait |
| "Storage response code: 200" | ✅ Upload success | Good |
| "Response code: 403" | ❌ Permission denied | Check policies |
| "Response code: 401" | ❌ Auth failed | Re-login |
| "Upload error:" | ❌ General error | Check error message |

