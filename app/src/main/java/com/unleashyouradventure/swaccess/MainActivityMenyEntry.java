package com.unleashyouradventure.swaccess;


enum MainActivityMenyEntry {
    myLibrary(R.string.Main_Activity_Item_Library, android.R.drawable.ic_menu_gallery),
    search(R.string.Main_Activity_Item_Search, android.R.drawable.ic_menu_search),
    category(R.string.Main_Activity_Item_ByCategory, android.R.drawable.ic_menu_compass),
    settings(R.string.Main_Activity_Item_Preferences, android.R.drawable.ic_menu_preferences),
    reader(R.string.Main_Activity_Item_Readers, android.R.drawable.ic_menu_preferences),
    about(R.string.Main_Activity_Item_about, android.R.drawable.ic_menu_info_details),
    help(R.string.Main_Activity_Item_help, android.R.drawable.ic_menu_help);
    private final int textId;
    private final int iconId;

    MainActivityMenyEntry(int textId, int iconId) {
        this.textId = textId;
        this.iconId = iconId;
    }

    public int getTextId() {
        return textId;
    }

    public int getIconId() {
        return iconId;
    }

}
