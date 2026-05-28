package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Elite Construction Visual Identity - Argenis Paredes style
// Features a heavy steel titanium structural look with a bright brilliant ruby red glow accent,
// and metallic warm copper/bronze titles for "Servicios en construcción en general".

val RubyRed = Color(0xFFE53935)       // Brand Central Gem Red
val RubyRedGlow = Color(0xFFFF2D37)   // Bright Gem Highlight
val RubyRedDark = Color(0xFF9E1B1B)   // Gem Shaded Area

val DarkSteel = Color(0xFF20252D)      // Titanium Metal Dark Gray
val ConcreteGrey = Color(0xFF64748B)   // Construction cement
val LightGrey = Color(0xFFF1F5F9)

// Copper & Bronze Metallic Accents (matching "Servicios en..." text)
val BronzeMetal = Color(0xFFC6996D)    // Subtitle Gold/Bronze
val BronzeLight = Color(0xFFE4C3A3)    // Light reflection
val BronzeDark = Color(0xFF865F3B)     // Shaded Bronze

val DarkBackground = Color(0xFF0F1219)  // Slate-Dark architectural ambient
val DarkSurface = Color(0xFF1B1E26)     // Card background
val LightBackground = Color(0xFFF4F6F9) // Off-white concrete reflection
val LightSurface = Color(0xFFFFFFFF)

// Keep safety mappings to prevent any compile break errors while matching the new branding
val SafetyOrange = RubyRed
val BrightOrange = RubyRedGlow
val DarkSlateNavy = DarkBackground
val SolidCharcoal = DarkSteel
val CardBackground = LightSurface
val PrimaryColor = RubyRed
val SecondaryColor = DarkSteel
val TertiaryColor = ConcreteGrey
