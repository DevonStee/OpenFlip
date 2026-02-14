package com.bokehforu.openflip.core.ui

/**
 * Centralized test tag constants for Compose UI automation.
 * These locale-independent IDs allow UI Automator / ADB to target
 * specific components without relying on displayed text.
 */
object TestTags {

    // Main screen
    const val MAIN_OPTIONS_BUTTON = "main_options_button"

    // Settings sheet
    const val SETTINGS_SHEET = "settings_sheet"
    const val SETTINGS_HEADER_BACK = "settings_header_back"
    const val SETTINGS_HEADER_CLOSE = "settings_header_close"

    // Time Display section
    const val SWITCH_SHOW_SECONDS = "settings_switch_show_seconds"
    const val NAV_TIME_FORMAT = "settings_nav_time_format"

    // Appearance section
    const val SWITCH_SHOW_FLAPS = "settings_switch_show_flaps"
    const val SWITCH_SWIPE_TO_DIM = "settings_switch_swipe_to_dim"
    const val SWITCH_LIGHT_MODE = "settings_switch_light_mode"
    const val NAV_ORIENTATION = "settings_nav_orientation"
    const val SWITCH_PINCH_SCALE = "settings_switch_pinch_scale"
    const val SWITCH_TIMED_BULB_OFF = "settings_switch_timed_bulb_off"

    // Screen Wake section
    const val NAV_KEEP_SCREEN_ON = "settings_nav_keep_screen_on"
    const val SWITCH_OLED_PROTECTION = "settings_switch_oled_protection"

    // Screensaver section
    const val NAV_SCREENSAVER = "settings_nav_screensaver"

    // Sleep Timer section
    const val NAV_SLEEP_TIMER = "settings_nav_sleep_timer"

    // Feedback section
    const val SWITCH_HAPTIC = "settings_switch_haptic"
    const val SWITCH_SOUND = "settings_switch_sound"
    const val SWITCH_HOURLY_CHIME = "settings_switch_hourly_chime"
    const val ACTION_TEST_CHIME = "settings_action_test_chime"

    // Actions
    const val ACTION_RESET = "settings_action_reset"
    const val ACTION_QUIT = "settings_action_quit"

    // Information section
    const val NAV_VERSION = "settings_nav_version"
    const val NAV_ABOUT = "settings_nav_about"
    const val NAV_ORIGINAL_APP = "settings_nav_original_app"
    const val NAV_CONTACT = "settings_nav_contact"

    // Sub-pages: Time Format
    const val RADIO_12H = "settings_radio_12h"
    const val RADIO_24H_00 = "settings_radio_24h_00"
    const val RADIO_24H_0 = "settings_radio_24h_0"

    // Sub-pages: Orientation
    const val RADIO_ORIENT_AUTO = "settings_radio_orient_auto"
    const val RADIO_ORIENT_PORTRAIT = "settings_radio_orient_portrait"
    const val RADIO_ORIENT_LAND_LEFT = "settings_radio_orient_land_left"
    const val RADIO_ORIENT_LAND_RIGHT = "settings_radio_orient_land_right"

    // Sub-pages: Wake Lock
    const val RADIO_WAKE_ALWAYS = "settings_radio_wake_always"
    const val RADIO_WAKE_CHARGING = "settings_radio_wake_charging"
    const val RADIO_WAKE_SYSTEM = "settings_radio_wake_system"

    // Sub-pages: Sleep Timer
    const val ACTION_SLEEP_15 = "settings_action_sleep_15"
    const val ACTION_SLEEP_30 = "settings_action_sleep_30"
    const val ACTION_SLEEP_60 = "settings_action_sleep_60"
    const val ACTION_SLEEP_120 = "settings_action_sleep_120"
    const val ACTION_SLEEP_180 = "settings_action_sleep_180"
    const val ACTION_SLEEP_CUSTOM = "settings_action_sleep_custom"
    const val ACTION_STOP_TIMER = "settings_action_stop_timer"
}
