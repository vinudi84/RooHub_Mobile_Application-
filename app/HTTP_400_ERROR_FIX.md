# HTTP 400 Error - Quick Fix Guide

## The Error You're Seeing
**"HTTP 400 failed status"** = Bad Request - something in the upload request is wrong

---

## 🚨 URGENT: Get the Full Error Message

Your error message got cut off. You need the FULL error to fix it. Here's how:

### Step 1: Run the app in Android Studio

1. Open **Android Studio**
2. Open your project
3. Click "Run" or press **Shift + F10**
4. Wait for app to start

### Step 2: Open the Logcat

1. At bottom of Android Studio, find **"Logcat"** tab
2. If you don't see it, go to: **View** > **Tool Windows** > **Logcat**

### Step 3: Try uploading and capture the error

1. In the app, try uploading a video
2. In Logcat, you'll see lots of messages
3. **LOOK FOR THESE LINES:**

```
E/CourseFormActivity: === VIDEO UPLOAD FAILED ===
E/CourseFormActivity: Response code: 400
E/CourseFormActivity: Response: {...}
```

OR

```
E/RestApiClient: === POST ERROR ===
E/RestApiClient: Status: 400
E/RestApiClient: Response: {...}
```

### Step 4: Copy the response

The `Response: {...}` line contains the actual error. It will say something like:
- `{"error":"bucket not found"}`
- `{"error":"invalid apikey"}`
- `{"error":"Unauthorized"}`
- etc.

**COPY this entire response and tell me what it says**

---

## 🔍 Alternative: Check Android Studio's Network Monitor

1. Go to **View** > **Tool Windows** > **Profiler**
2. Click the **Network** tab
3. Try uploading
4. Click on the failed request
5. Look at **Response** tab

---

## Common Response Errors & Fixes

### Error 1: `"bucket not found"`
**What's wrong:** The `course-videos` bucket doesn't exist in Supabase

**Fix:**
1. Go to Supabase Console > Storage
2. Click "Create new bucket"
3. Name: `course-videos`
4. Public: Yes
5. Size limit: 500MB
6. Click Create

---

### Error 2: `"invalid apikey"`
**What's wrong:** The Supabase API key is wrong

**Fix:**
1. Check [SupabaseClient.kt](src/main/java/com/example/roohub/SupabaseClient.kt)
2. Copy your real keys from Supabase Console
3. Replace the placeholder keys

---

### Error 3: `"Unauthorized"` or `"unauthorized"`
**What's wrong:** Auth token is missing or expired

**Fix:**
1. Log out of the app
2. Log back in
3. Try uploading again

---

### Error 4: `"failed to read body"`
**What's wrong:** Video file is corrupted or can't be read

**Fix:**
1. Try with a different video file
2. Check video plays in your phone's video player
3. Try shorter video (under 100MB)

---

### Error 5: `"request entity too large"`
**What's wrong:** Video file is too big

**Fix:**
1. Compress the video (see guide below)
2. Max size: 500MB
3. Recommended: Under 100MB

---

## 🎥 How to Compress a Video

### Using Online Tools (Easiest):
1. Go to https://www.online-convert.com/
2. Select "Convert to MP4"
3. Upload video
4. Set quality to "Good"
5. Download compressed video

### Using FFmpeg (Command Line):
```bash
ffmpeg -i input.mp4 -vf scale=1280:-1 -b:v 5M -c:a aac output.mp4
```

### Using HandBrake (Desktop App):
1. Download HandBrake
2. Open video file
3. Set destination: "Fast 720p30"
4. Click Start
5. Wait for conversion

---

## 📋 DOES Your Supabase Setup Have Everything?

Run this checklist:

- [ ] `course-videos` bucket exists in Storage
- [ ] Bucket is PUBLIC
- [ ] You can see the bucket in Supabase console
- [ ] `course_uploads` table exists in Database
- [ ] RLS policies are configured
- [ ] Supabase API credentials match in SupabaseClient.kt

If any of these are missing, fix them first!

---

## 🚀 Step-by-Step To Fix

1. **Get the FULL error message** from Logcat
2. **Search for it above** in this guide
3. **Apply the fix**
4. **Rebuild** the app (Build > Rebuild Project)
5. **Try upload again**

---

## 📞 Need More Help?

**Share with me:**
1. The full `Response: {...}` message from Logcat
2. A screenshot of the error in Logcat
3. Confirm you:
   - [ ] Created `course-videos` bucket
   - [ ] Set it to PUBLIC
   - [ ] Are logged in successfully
   - [ ] Selected a valid MP4 video under 500MB

---

## 🔧 Also Try This Right Now

1. **Rebuild the app:**
   - Android Studio > Build > Rebuild Project
   - Wait until it finishes

2. **Clear app data:**
   - Settings > Apps > RooHub > Clear Data
   - Or: adb shell pm clear com.example.roohub

3. **Log out and back in:**
   - Exit the app
   - Remove from recent apps
   - Open fresh
   - Log in again

4. **Try uploading:**
   - Select a small test video (under 50MB)
   - Check Logcat for full error
   - Share the error with me

