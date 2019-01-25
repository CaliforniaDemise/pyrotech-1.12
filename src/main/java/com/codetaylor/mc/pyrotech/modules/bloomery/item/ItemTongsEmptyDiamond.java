package com.codetaylor.mc.pyrotech.modules.bloomery.item;

import com.codetaylor.mc.pyrotech.modules.bloomery.ModuleBloomery;
import com.codetaylor.mc.pyrotech.modules.bloomery.ModuleBloomeryConfig;

public class ItemTongsEmptyDiamond
    extends ItemTongsEmptyBase {

  public static final String NAME = "tongs_diamond";

  public ItemTongsEmptyDiamond() {

    super(() -> ModuleBloomery.Items.TONGS_DIAMOND_FULL, ModuleBloomeryConfig.TONGS.DIAMOND_TONGS_DURABILITY);
  }
}