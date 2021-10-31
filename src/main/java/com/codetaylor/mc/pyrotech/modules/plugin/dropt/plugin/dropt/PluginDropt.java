package com.codetaylor.mc.pyrotech.modules.plugin.dropt.plugin.dropt;

import com.codetaylor.mc.dropt.api.api.IDroptDropBuilder;
import com.codetaylor.mc.dropt.api.api.IDroptRuleBuilder;
import com.codetaylor.mc.dropt.api.event.DroptLoadRulesEvent;
import com.codetaylor.mc.dropt.api.reference.*;
import com.codetaylor.mc.pyrotech.ModPyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.core.block.*;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemMaterial;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemStrangeTuber;
import com.codetaylor.mc.pyrotech.modules.plugin.dropt.ModulePluginDropt;
import com.codetaylor.mc.pyrotech.modules.plugin.dropt.ModulePluginDroptConfig;
import com.codetaylor.mc.pyrotech.modules.tool.item.ItemCrudePickaxe;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStone;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.codetaylor.mc.dropt.api.DroptAPI.*;

public class PluginDropt {

  public PluginDropt() {

    MinecraftForge.EVENT_BUS.register(this);
  }

  private static String item(String modId, String name) {

    return item(modId, name, 0);
  }

  private static String item(String name, int meta) {

    return item(ModulePluginDropt.MOD_ID, name, meta);
  }

  private static String item(String modId, String name, int meta) {

    return itemString(modId, name, meta);
  }

  private static boolean enabled(String key) {

    return ModulePluginDroptConfig.ENABLED_RULES.containsKey(key)
        && ModulePluginDroptConfig.ENABLED_RULES.get(key);
  }

  @SubscribeEvent
  public void on(DroptLoadRulesEvent event) {

    if (!ModPyrotechConfig.MODULES.get("plugin.dropt")) {
      return;
    }

    // -------------------------------------------------------------------------
    // - Item / Block Strings
    // -------------------------------------------------------------------------

    String sand = item("minecraft", "sand");
    String sandRed = item("minecraft", "sand", BlockSand.EnumType.RED_SAND.getMetadata());
    String dirtAny = item("minecraft", "dirt", OreDictionary.WILDCARD_VALUE);
    String farmlandAny = item("minecraft", "farmland", OreDictionary.WILDCARD_VALUE);
    String mycelium = item("minecraft", "mycelium");
    String dirt = item("minecraft", "dirt", BlockDirt.DirtType.DIRT.getMetadata());
    String grass = item("minecraft", "grass");
    String grassPath = item("minecraft", "grass_path");
    String gravel = item("minecraft", "gravel");
    String tallGrassAny = item("minecraft", "tallgrass", OreDictionary.WILDCARD_VALUE);
    String coalOre = item("minecraft", "coal_ore");
    String clay = item("minecraft", "clay");
    String cobblestone = item("minecraft", "cobblestone");

    String flint = item("minecraft", "flint");
    String boneMeal = item("minecraft", "dye", 15);
    String coal = item("minecraft", "coal", 0);
    String clayBall = item("minecraft", "clay_ball", 0);

    String fossilOre = item(BlockOreFossil.NAME, 0);
    String rockStone = item(BlockRock.NAME, BlockRock.EnumType.STONE.getMeta());
    String rockGranite = item(BlockRock.NAME, BlockRock.EnumType.GRANITE.getMeta());
    String rockDiorite = item(BlockRock.NAME, BlockRock.EnumType.DIORITE.getMeta());
    String rockAndesite = item(BlockRock.NAME, BlockRock.EnumType.ANDESITE.getMeta());
    String rockDirt = item(BlockRock.NAME, BlockRock.EnumType.DIRT.getMeta());
    String rockMud = item(BlockRock.NAME, BlockRock.EnumType.MUD.getMeta());
    String rockSand = item(BlockRock.NAME, BlockRock.EnumType.SAND.getMeta());
    String rockSandRed = item(BlockRock.NAME, BlockRock.EnumType.SAND_RED.getMeta());
    String rockGrass = item(BlockRockGrass.NAME, 0);
    String cobbledAndesite = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.ANDESITE.getMeta());
    String cobbledDiorite = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.DIORITE.getMeta());
    String cobbledGranite = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.GRANITE.getMeta());
    String cobbledLimestone = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.LIMESTONE.getMeta());
    String mulchedFarmland = item(BlockFarmlandMulched.NAME, 0);
    String mud = item(BlockMud.NAME, 0);

    String flintShard = item(ItemMaterial.NAME, ItemMaterial.EnumType.FLINT_SHARD.getMeta());
    String boneShard = item(ItemMaterial.NAME, ItemMaterial.EnumType.BONE_SHARD.getMeta());
    String plantFibers = item(ItemMaterial.NAME, ItemMaterial.EnumType.PLANT_FIBERS.getMeta());
    String plantFibersDried = item(ItemMaterial.NAME, ItemMaterial.EnumType.PLANT_FIBERS_DRIED.getMeta());
    String coalPieces = item(ItemMaterial.NAME, ItemMaterial.EnumType.COAL_PIECES.getMeta());
    String clayLump = item(ItemMaterial.NAME, ItemMaterial.EnumType.CLAY_LUMP.getMeta());
    String strangeTuber = item(ItemStrangeTuber.NAME, 0);

    List<IDroptRuleBuilder> list = new ArrayList<>();

    // -------------------------------------------------------------------------
    // - Tall Grass
    // -------------------------------------------------------------------------

    if (enabled("grass_tall")) {
      list.add(rule()
          .matchBlocks(new String[]{
              tallGrassAny
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.WHITELIST, new String[]{"EMPTY"})
          )
          .replaceStrategy(EnumReplaceStrategy.ADD)
          .addDrops(new IDroptDropBuilder[]{
              drop().selector(weight(80)),
              drop().items(new String[]{plantFibers}, range(1, 2)).selector(weight(35)),
              drop().items(new String[]{plantFibersDried}, range(1, 2)).selector(weight(5)),
              drop().items(new String[]{strangeTuber}, range(1, 2)).selector(weight(2))
          })
      );
      list.add(rule()
          .matchBlocks(new String[]{
              tallGrassAny
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
          )
          .replaceStrategy(EnumReplaceStrategy.ADD)
          .addDrops(new IDroptDropBuilder[]{
              drop().selector(weight(80)),
              drop().items(new String[]{plantFibers}, range(1, 2)).selector(weight(35)),
              drop().items(new String[]{plantFibersDried}, range(1, 2)).selector(weight(5))
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Dirt
    // -------------------------------------------------------------------------

    if (enabled("dirt")) {

      if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
        // Explosion
        // Drops dirt clumps
        list.add(rule()
            .matchBlocks(new String[]{
                dirtAny,
                farmlandAny,
                mulchedFarmland,
                mycelium
            })
            .matchHarvester(harvester()
                .type(EnumHarvesterType.EXPLOSION)
            )
            .addDrops(new IDroptDropBuilder[]{
                drop().selector(weight(9)),
                drop().items(new String[]{rockDirt}, range(1))
            })
        );
      }

      // Not a shovel
      // Drops dirt clumps
      list.add(rule()
          .matchBlocks(new String[]{
              dirtAny,
              farmlandAny,
              mulchedFarmland,
              mycelium
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockDirt}, range(1, 3))
          })
      );

      // Shovel 0
      // Drops dirt clumps
      list.add(rule()
          .matchBlocks(new String[]{
              dirtAny,
              farmlandAny,
              mulchedFarmland,
              mycelium
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(1, 2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockDirt}, range(2, 4))
          })
      );

      // Shovel 1
      // Drops dirt clumps
      list.add(rule()
          .matchBlocks(new String[]{
              dirtAny,
              farmlandAny,
              mulchedFarmland,
              mycelium
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockDirt}, range(3, 6)).selector(weight(4)),
              drop().items(new String[]{dirt})
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Mud
    // -------------------------------------------------------------------------

    if (enabled("mud")) {

      if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
        // Explosion
        // Drops mud clumps
        list.add(rule()
            .matchBlocks(new String[]{
                mud
            })
            .matchHarvester(harvester()
                .type(EnumHarvesterType.EXPLOSION)
            )
            .addDrops(new IDroptDropBuilder[]{
                drop().selector(weight(9)),
                drop().items(new String[]{rockMud}, range(1))
            })
        );
      }

      // Not a shovel
      // Drops mud clumps
      list.add(rule()
          .matchBlocks(new String[]{
              mud
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockMud}, range(1, 3))
          })
      );

      // Shovel 0
      // Drops mud clumps
      list.add(rule()
          .matchBlocks(new String[]{
              mud
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(1, 2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockMud}, range(2, 4))
          })
      );

      // Shovel 1
      // Drops mud clumps
      list.add(rule()
          .matchBlocks(new String[]{
              mud
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockMud}, range(3, 6)).selector(weight(4)),
              drop().items(new String[]{mud})
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Grass
    // -------------------------------------------------------------------------

    if (enabled("grass")) {

      if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
        // Explosion
        // Drops dirt clumps
        list.add(rule()
            .matchBlocks(new String[]{
                grass,
                grassPath
            })
            .matchHarvester(harvester()
                .type(EnumHarvesterType.EXPLOSION)
            )
            .addDrops(new IDroptDropBuilder[]{
                drop().selector(weight(9)),
                drop().items(new String[]{rockDirt}, range(1))
            })
        );
      }

      // Empty hand
      // Drops dirt clumps and strange tubers
      list.add(rule()
          .matchBlocks(new String[]{
              grass,
              grassPath
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.WHITELIST, new String[]{"EMPTY"})
          )
          .dropCount(range(1, 2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockDirt}, range(1, 3)).selector(weight(5)),
              drop().items(new String[]{strangeTuber}, range(1, 2)).selector(weight(2))
          })
      );

      // Not a shovel
      // Drops dirt clumps
      list.add(rule()
          .matchBlocks(new String[]{
              grass,
              grassPath
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .dropCount(range(1, 2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockDirt}, range(1, 3)).selector(weight(50)),
              drop().items(new String[]{strangeTuber}).selector(weight(1))
          })
      );

      // Shovel 0
      // Drops dirt clumps, grass clumps, and Strange Tubers
      list.add(rule()
          .matchBlocks(new String[]{
              grass,
              grassPath
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(1, 2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockDirt}, range(2, 4)).selector(weight(400)),
              drop().items(new String[]{rockGrass}).selector(weight(100)),
              drop().items(new String[]{strangeTuber}).selector(weight(5))
          })
      );

      // Shovel 1
      // Drops dirt clumps and grass clumps
      list.add(rule()
          .matchBlocks(new String[]{
              grass,
              grassPath
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{dirt}).selector(weight(100)),
              drop().items(new String[]{rockDirt}, range(1, 3)).selector(weight(300)),
              drop().items(new String[]{rockGrass}, range(1, 3)).selector(weight(200)),
              drop().items(new String[]{strangeTuber}).selector(weight(1))
          })
      );

      // Shovel Any
      // Adds grass clumps to drops
      list.add(rule()
          .matchBlocks(new String[]{
              grass,
              grassPath
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand("shovel;-1;-1")
          )
          .replaceStrategy(EnumReplaceStrategy.ADD)
          .dropCount(range(0, 1))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockGrass}, range(1, 3)).selector(weight(1), EnumSilktouch.EXCLUDED)
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Sand
    // -------------------------------------------------------------------------

    if (enabled("sand")) {

      if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
        // Explosion
        list.add(rule()
            .matchBlocks(new String[]{
                sand
            })
            .matchHarvester(harvester()
                .type(EnumHarvesterType.EXPLOSION)
            )
            .addDrops(new IDroptDropBuilder[]{
                drop().selector(weight(9)),
                drop().items(new String[]{rockSand}, range(1))
            })
        );
      }

      // Not a shovel
      list.add(rule()
          .matchBlocks(new String[]{
              sand
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockSand}, range(1, 3))
          })
      );

      // Shovel 0
      list.add(rule()
          .matchBlocks(new String[]{
              sand
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockSand}, range(2, 4))
          })
      );

      // Shovel 1
      list.add(rule()
          .matchBlocks(new String[]{
              sand
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockSand}, range(3, 6)).selector(weight(4)),
              drop().items(new String[]{sand})
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Red Sand
    // -------------------------------------------------------------------------

    if (enabled("sand_red")) {

      if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
        // Explosion
        list.add(rule()
            .matchBlocks(new String[]{
                sandRed
            })
            .matchHarvester(harvester()
                .type(EnumHarvesterType.EXPLOSION)
            )
            .addDrops(new IDroptDropBuilder[]{
                drop().selector(weight(9)),
                drop().items(new String[]{rockSandRed}, range(1))
            })
        );
      }

      // Not a shovel
      list.add(rule()
          .matchBlocks(new String[]{
              sandRed
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockSandRed}, range(1, 3))
          })
      );

      // Shovel 0
      list.add(rule()
          .matchBlocks(new String[]{
              sandRed
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockSandRed}, range(2, 4))
          })
      );

      // Shovel 1
      list.add(rule()
          .matchBlocks(new String[]{
              sandRed
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockSandRed}, range(3, 6)).selector(weight(4)),
              drop().items(new String[]{sandRed})
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Gravel
    // -------------------------------------------------------------------------

    if (enabled("gravel")) {

      if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
        // Explosion
        list.add(rule()
            .matchBlocks(new String[]{
                gravel
            })
            .matchHarvester(harvester()
                .type(EnumHarvesterType.EXPLOSION)
            )
            .addDrops(new IDroptDropBuilder[]{
                drop().selector(weight(360)),
                drop().items(new String[]{rockStone}, range(1)).selector(weight(10)),
                drop().items(new String[]{rockGranite}, range(1)).selector(weight(10)),
                drop().items(new String[]{rockDiorite}, range(1)).selector(weight(10)),
                drop().items(new String[]{rockAndesite}, range(1)).selector(weight(10))
            })
        );
      }

      // Not a shovel
      list.add(rule()
          .matchBlocks(new String[]{
              gravel
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(1, 3)).selector(weight(10)),
              drop().items(new String[]{rockGranite}, range(1, 3)).selector(weight(10)),
              drop().items(new String[]{rockDiorite}, range(1, 3)).selector(weight(10)),
              drop().items(new String[]{rockAndesite}, range(1, 3)).selector(weight(10))
          })
      );

      // Shovel 0
      list.add(rule()
          .matchBlocks(new String[]{
              gravel
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .dropCount(range(2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(1, 2)).selector(weight(2)),
              drop().items(new String[]{rockGranite}, range(1, 2)).selector(weight(2)),
              drop().items(new String[]{rockDiorite}, range(1, 2)).selector(weight(2)),
              drop().items(new String[]{rockAndesite}, range(1, 2)).selector(weight(2)),
              drop().items(new String[]{flintShard}).selector(weight(1))
          })
      );

      // Shovel 1
      list.add(rule()
          .matchBlocks(new String[]{
              gravel
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .dropCount(range(2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(1, 3)).selector(weight(1)),
              drop().items(new String[]{rockGranite}, range(1, 3)).selector(weight(1)),
              drop().items(new String[]{rockDiorite}, range(1, 3)).selector(weight(1)),
              drop().items(new String[]{rockAndesite}, range(1, 3)).selector(weight(1)),
              drop().items(new String[]{gravel}).selector(weight(2)),
              drop().items(new String[]{flintShard}).selector(weight(2)),
              drop().items(new String[]{flint}).selector(weight(1))
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Clay
    // -------------------------------------------------------------------------

    if (enabled("clay")) {

      // Not a shovel
      list.add(rule()
          .matchBlocks(new String[]{
              clay
          })
          .matchHarvester(harvester()
              .mainHand(EnumListType.BLACKLIST, "shovel;0;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{clayLump}, range(1, 3))
          })
      );

      // Shovel 0
      list.add(rule()
          .matchBlocks(new String[]{
              clay
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;1;-1")
          )
          .dropCount(range(2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{clayLump}, range(1, 2)).selector(weight(90)),
              drop().items(new String[]{clayBall}, range(1, 2)).selector(weight(10))
          })
      );

      // Shovel 1
      list.add(rule()
          .matchBlocks(new String[]{
              clay
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "shovel;2;-1")
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{clayLump}, range(1, 4)).selector(weight(40)),
              drop().items(new String[]{clayBall}, range(1, 2)).selector(weight(30)),
              drop().items(new String[]{clayBall}, range(1, 2)).selector(weight(30))
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Coal
    // -------------------------------------------------------------------------

    if (enabled("ore_coal")) {

      // Non-Player
      list.add(rule()
          .matchBlocks(new String[]{
              coalOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.NON_PLAYER)
          )
          .dropCount(range(2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(2, 4)).selector(weight(8)),
              drop().items(new String[]{coalPieces}, range(1, 2)).selector(weight(1))
          })
      );

      // Crude Pickaxe
      list.add(rule()
          .matchBlocks(new String[]{
              coalOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(new String[]{
                  item(ItemCrudePickaxe.NAME, OreDictionary.WILDCARD_VALUE)
              })
          )
          .dropCount(range(2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(2, 4)).selector(weight(8)),
              drop().items(new String[]{coalPieces}, range(1, 2)).selector(weight(1))
          })
      );

      // Flint / Bone / Stone Pickaxe
      list.add(rule()
          .matchBlocks(new String[]{
              coalOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "pickaxe;2;-1")
          )
          .dropCount(range(2))
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(1, 2)).selector(weight(8)),
              drop().items(new String[]{coalPieces}, range(2, 4)).selector(weight(8))
          })
      );

      // Iron Pickaxe
      list.add(rule()
          .matchBlocks(new String[]{
              coalOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "pickaxe;3;-1")
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{coalPieces}, range(4, 8, 4)).selector(weight(35)),
              drop().items(new String[]{coal}, range(1, 1)).selector(weight(65, 1))
          })
      );

      // Diamond+ Pickaxe
      list.add(rule()
          .matchBlocks(new String[]{
              coalOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "pickaxe;4;-1")
          )
          .replaceStrategy(EnumReplaceStrategy.ADD)
          .addDrops(new IDroptDropBuilder[]{
              drop().selector(weight(1)),
              drop().items(new String[]{coal}, range(1, 1)).selector(weight(1, 1), EnumSilktouch.EXCLUDED)
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Fossil Ore
    // -------------------------------------------------------------------------

    if (enabled("ore_fossil")) {

      // Non-Player
      list.add(rule()
          .matchBlocks(new String[]{
              fossilOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.NON_PLAYER)
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(2, 4)).selector(weight(8)),
              drop().items(new String[]{boneShard}, range(1, 2)).selector(weight(1))
          })
      );

      // Crude Pickaxe
      // Drops rocks
      list.add(rule()
          .matchBlocks(new String[]{
              fossilOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(new String[]{
                  item(ItemCrudePickaxe.NAME, OreDictionary.WILDCARD_VALUE)
              })
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(2, 4)).selector(weight(8)),
              drop().items(new String[]{boneShard}, range(1)).selector(weight(1))
          })
      );

      // Flint / Bone / Stone Pickaxe
      // Drops rocks
      list.add(rule()
          .matchBlocks(new String[]{
              fossilOre
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.PLAYER)
              .mainHand(EnumListType.BLACKLIST, "pickaxe;2;-1")
          )
          .dropStrategy(EnumDropStrategy.UNIQUE)
          .dropCount(range(2))
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{rockStone}, range(2, 4)).selector(weight(6)),
              drop().items(new String[]{boneMeal}, range(1)).selector(weight(1)),
              drop().items(new String[]{boneShard}, range(2, 3)).selector(weight(2))
          })
      );

      // Tier 2+
      list.add(rule()
          .matchBlocks(new String[]{
              fossilOre
          })
          .replaceStrategy(EnumReplaceStrategy.ADD)
          .addDrops(new IDroptDropBuilder[]{
              drop().items(new String[]{cobblestone, rockStone}, range(3, 6)).selector(weight(4), EnumSilktouch.EXCLUDED),
              drop().items(new String[]{boneShard}, range(2, 4)).selector(weight(5), EnumSilktouch.EXCLUDED)
          })
      );
    }

    // -------------------------------------------------------------------------
    // - Sandstone
    // -------------------------------------------------------------------------

    if (enabled("sandstone")) {
      String matchBlock = item("minecraft", "sandstone", OreDictionary.WILDCARD_VALUE);
      String rock = item(BlockRock.NAME, BlockRock.EnumType.SANDSTONE.getMeta());
      this.addRockDrops(matchBlock, rock, list);
    }

    // -------------------------------------------------------------------------
    // - Red Sandstone
    // -------------------------------------------------------------------------

    if (enabled("sandstone_red")) {
      String matchBlock = item("minecraft", "red_sandstone", OreDictionary.WILDCARD_VALUE);
      String rock = item(BlockRock.NAME, BlockRock.EnumType.SANDSTONE_RED.getMeta());
      this.addRockDrops(matchBlock, rock, list);
    }

    // -------------------------------------------------------------------------
    // - Limestone
    // -------------------------------------------------------------------------

    if (enabled("limestone")) {
      String matchBlock = item("pyrotech", "limestone");
      String rock = item(BlockRock.NAME, BlockRock.EnumType.LIMESTONE.getMeta());
      this.addRockDrops(matchBlock, rock, list);
    }

    if (enabled("limestone_cobbled")) {
      String rock = item(BlockRock.NAME, BlockRock.EnumType.LIMESTONE.getMeta());
      this.addRockDrops(cobbledLimestone, rock, cobbledLimestone, list);
      this.addBlockReplace(cobbledLimestone, cobbledLimestone, list);
    }

    // -------------------------------------------------------------------------
    // - Netherrack
    // -------------------------------------------------------------------------

    if (enabled("netherrack")) {
      String matchBlock = item("minecraft", "netherrack", BlockStone.EnumType.STONE.getMetadata());
      String rock = item(BlockRockNetherrack.NAME, 0);
      this.addRockDrops(matchBlock, rock, list);
    }

    // -------------------------------------------------------------------------
    // - Stone / Cobblestone
    // -------------------------------------------------------------------------

    if (enabled("stone")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.STONE.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.STONE.getMeta());
      this.addRockDrops(matchBlock, rock, list);
    }

    if (enabled("cobblestone")) {
      String matchBlock = item("minecraft", "cobblestone");
      String rock = item(BlockRock.NAME, BlockRock.EnumType.STONE.getMeta());
      this.addRockDrops(matchBlock, rock, list);
    }

    // -------------------------------------------------------------------------
    // - Diorite
    // -------------------------------------------------------------------------

    if (enabled("diorite")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.DIORITE.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.DIORITE.getMeta());
      String replaceBlock = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.DIORITE.getMeta());
      this.addRockDrops(matchBlock, rock, replaceBlock, list);
      this.addBlockReplace(matchBlock, replaceBlock, list);
    }

    if (enabled("diorite_smooth")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.DIORITE_SMOOTH.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.DIORITE.getMeta());
      String replaceBlock = item("minecraft", "stone", BlockStone.EnumType.DIORITE.getMetadata());
      this.addRockDrops(matchBlock, rock, replaceBlock, list);
      this.addBlockReplace(matchBlock, replaceBlock, list);
    }

    if (enabled("diorite_cobbled")) {
      String rock = item(BlockRock.NAME, BlockRock.EnumType.DIORITE.getMeta());
      this.addRockDrops(cobbledDiorite, rock, cobbledDiorite, list);
      this.addBlockReplace(cobbledDiorite, cobbledDiorite, list);
    }

    // -------------------------------------------------------------------------
    // - Andesite
    // -------------------------------------------------------------------------

    if (enabled("andesite")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.ANDESITE.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.ANDESITE.getMeta());
      String replaceBlock = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.ANDESITE.getMeta());
      this.addRockDrops(matchBlock, rock, replaceBlock, list);
      this.addBlockReplace(matchBlock, replaceBlock, list);
    }

    if (enabled("andesite_smooth")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.ANDESITE.getMeta());
      String replaceBlock = item("minecraft", "stone", BlockStone.EnumType.ANDESITE.getMetadata());
      this.addRockDrops(matchBlock, rock, replaceBlock, list);
      this.addBlockReplace(matchBlock, replaceBlock, list);
    }

    if (enabled("andesite_cobbled")) {
      String rock = item(BlockRock.NAME, BlockRock.EnumType.ANDESITE.getMeta());
      this.addRockDrops(cobbledAndesite, rock, cobbledAndesite, list);
      this.addBlockReplace(cobbledAndesite, cobbledAndesite, list);
    }

    // -------------------------------------------------------------------------
    // - Granite
    // -------------------------------------------------------------------------

    if (enabled("granite")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.GRANITE.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.GRANITE.getMeta());
      String replaceBlock = item(BlockCobblestone.NAME, BlockCobblestone.EnumType.GRANITE.getMeta());
      this.addRockDrops(matchBlock, rock, replaceBlock, list);
      this.addBlockReplace(matchBlock, replaceBlock, list);
    }

    if (enabled("granite_smooth")) {
      String matchBlock = item("minecraft", "stone", BlockStone.EnumType.GRANITE_SMOOTH.getMetadata());
      String rock = item(BlockRock.NAME, BlockRock.EnumType.GRANITE.getMeta());
      String replaceBlock = item("minecraft", "stone", BlockStone.EnumType.GRANITE.getMetadata());
      this.addRockDrops(matchBlock, rock, replaceBlock, list);
      this.addBlockReplace(matchBlock, replaceBlock, list);
    }

    if (enabled("granite_cobbled")) {
      String rock = item(BlockRock.NAME, BlockRock.EnumType.GRANITE.getMeta());
      this.addRockDrops(cobbledGranite, rock, cobbledGranite, list);
      this.addBlockReplace(cobbledGranite, cobbledGranite, list);
    }

    // -------------------------------------------------------------------------
    // - Registration
    // -------------------------------------------------------------------------

    ResourceLocation resourceLocation = new ResourceLocation(ModulePluginDropt.MOD_ID, "dropt");
    registerRuleList(resourceLocation, 0, list);
  }

  private void addBlockReplace(String matchBlock, String replaceBlock, List<IDroptRuleBuilder> list) {

    list.add(rule()
        .matchBlocks(new String[]{
            matchBlock
        })
        .replaceStrategy(EnumReplaceStrategy.REPLACE_ALL_IF_SELECTED)
        .addDrops(new IDroptDropBuilder[]{
            drop().items(new String[]{replaceBlock}).selector(weight(1), EnumSilktouch.EXCLUDED)
        })
    );
  }

  private void addRockDrops(String matchBlock, String rock, List<IDroptRuleBuilder> list) {

    this.addRockDrops(matchBlock, rock, null, list);
  }

  private void addRockDrops(String matchBlock, String rock, @Nullable String replaceBlock, List<IDroptRuleBuilder> list) {

    if (ModulePluginDroptConfig.REDUCE_EXPLOSION_DROPS) {
      // Explosion
      list.add(rule()
          .matchBlocks(new String[]{
              matchBlock
          })
          .matchHarvester(harvester()
              .type(EnumHarvesterType.EXPLOSION)
          )
          .addDrops(new IDroptDropBuilder[]{
              drop().selector(weight(9)),
              drop().items(new String[]{rock}, range(1))
          })
      );
    }

    // Non-Player
    list.add(rule()
        .matchBlocks(new String[]{
            matchBlock
        })
        .matchHarvester(harvester()
            .type(EnumHarvesterType.NON_PLAYER)
        )
        .addDrops(new IDroptDropBuilder[]{
            drop().items(new String[]{rock}, range(2, 4))
        })
    );

    // Crude Pickaxe
    // Drops rocks
    list.add(rule()
        .matchBlocks(new String[]{
            matchBlock
        })
        .matchHarvester(harvester()
            .type(EnumHarvesterType.PLAYER)
            .mainHand(new String[]{
                item(ItemCrudePickaxe.NAME, OreDictionary.WILDCARD_VALUE)
            })
        )
        .addDrops(new IDroptDropBuilder[]{
            drop().items(new String[]{rock}, range(2, 4))
        })
    );

    // Flint / Bone / Stone Pickaxe
    // Drops rocks
    list.add(rule()
        .matchBlocks(new String[]{
            matchBlock
        })
        .matchHarvester(harvester()
            .type(EnumHarvesterType.PLAYER)
            .mainHand(EnumListType.BLACKLIST, "pickaxe;2;-1")
        )
        .replaceStrategy(EnumReplaceStrategy.REPLACE_ALL_IF_SELECTED)
        .addDrops(new IDroptDropBuilder[]{
            drop().items(new String[]{rock}, range(3, 6)).selector(weight(4)),
            (replaceBlock == null) ? drop() : drop().items(new String[]{replaceBlock})
        })
    );
  }
}
