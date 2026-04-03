package com.example.roohub

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
     const val SUPABASE_URL      = "https://ydjgoscchblkngpraubh.supabase.co"
     const val SUPABASE_ANON_KEY = "sb_publishable_ZpHQAM7r40AhrzaHjLCqxA_61gjEj7i" // ⚠️ paste real key

    @JvmStatic
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}