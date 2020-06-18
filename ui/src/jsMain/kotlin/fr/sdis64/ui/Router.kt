package fr.sdis64.ui

enum class Routes(val path: String) {
    LOGIN("/login"),
    SCREEN_CODIS("/screen/codis"),
    SCREEN_CTA("/screen/cta"),
    SCREEN_CRISIS("/screen/crisis"),
    SCREEN_SAMU("/screen/samu"),
    ADMIN_DASHBOARD_MANUAL_INDICATORS("/admin-dashboard/manual-indicators"),
    ADMIN_DASHBOARD_MOUNTAIN_RESCUE("/admin-dashboard/organisms");
}
