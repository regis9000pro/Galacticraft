package micdoodle8.mods.galacticraft.mars.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.EnumSet;
import java.util.Random;
import micdoodle8.mods.galacticraft.API.IGalacticraftSubModClient;
import micdoodle8.mods.galacticraft.API.IMapPlanet;
import micdoodle8.mods.galacticraft.API.IPlanetSlotRenderer;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.render.entities.GCCoreRenderArrow;
import micdoodle8.mods.galacticraft.core.client.render.entities.GCCoreRenderSpaceship;
import micdoodle8.mods.galacticraft.core.entities.GCCoreEntityArrow;
import micdoodle8.mods.galacticraft.core.util.PacketUtil;
import micdoodle8.mods.galacticraft.mars.CommonProxyMars;
import micdoodle8.mods.galacticraft.mars.blocks.GCMarsBlocks;
import micdoodle8.mods.galacticraft.mars.client.model.GCMarsModelSpaceshipTier2;
import micdoodle8.mods.galacticraft.mars.client.render.item.GCMarsItemRendererSpaceshipTier2;
import micdoodle8.mods.galacticraft.mars.dimension.GCMarsWorldProvider;
import micdoodle8.mods.galacticraft.mars.entities.GCCoreEntityRocketT2;
import micdoodle8.mods.galacticraft.mars.entities.GCMarsEntityCreeperBoss;
import micdoodle8.mods.galacticraft.mars.entities.GCMarsEntityProjectileTNT;
import micdoodle8.mods.galacticraft.mars.entities.GCMarsEntitySludgeling;
import micdoodle8.mods.galacticraft.mars.items.GCMarsItemJetpack;
import micdoodle8.mods.galacticraft.mars.items.GCMarsItems;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * Copyright 2012-2013, micdoodle8
 * 
 * All rights reserved.
 * 
 */
public class ClientProxyMars extends CommonProxyMars implements IGalacticraftSubModClient
{
    private static int fluidRenderID;
    public static long getFirstBootTime;
    public static long getCurrentTime;
    private final Random rand = new Random();

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new GCMarsSounds());
        ClientProxyMars.getFirstBootTime = System.currentTimeMillis();
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        GalacticraftCore.registerClientSubMod(this);
        TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
        NetworkRegistry.instance().registerChannel(new ClientPacketHandler(), "GalacticraftMars", Side.CLIENT);
        ClientProxyMars.fluidRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new GCMarsBlockRendererBacterialSludge(ClientProxyMars.fluidRenderID));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    @Override
    public void registerRenderInformation()
    {
        RenderingRegistry.registerEntityRenderingHandler(GCCoreEntityRocketT2.class, new GCCoreRenderSpaceship(new GCMarsModelSpaceshipTier2(), "/micdoodle8/mods/galacticraft/mars/client/entities/spaceshipTier2.png"));
        RenderingRegistry.registerEntityRenderingHandler(GCMarsEntityCreeperBoss.class, new GCMarsRenderCreeperBoss(new GCMarsModelCreeperBoss(), 10.0F));
        RenderingRegistry.registerEntityRenderingHandler(GCMarsEntitySludgeling.class, new GCMarsRenderSludgeling());
        RenderingRegistry.addNewArmourRendererPrefix("sensor");
        RenderingRegistry.addNewArmourRendererPrefix("sensorox");
        RenderingRegistry.addNewArmourRendererPrefix("quandrium");
        RenderingRegistry.addNewArmourRendererPrefix("quandriumox");
        RenderingRegistry.addNewArmourRendererPrefix("desh");
        RenderingRegistry.addNewArmourRendererPrefix("deshox");
        RenderingRegistry.addNewArmourRendererPrefix("heavy");
        RenderingRegistry.addNewArmourRendererPrefix("jetpack");
        RenderingRegistry.registerEntityRenderingHandler(GCCoreEntityArrow.class, new GCCoreRenderArrow());
        RenderingRegistry.registerEntityRenderingHandler(GCMarsEntityProjectileTNT.class, new GCMarsRenderProjectileTNT());
        MinecraftForgeClient.registerItemRenderer(GCMarsItems.spaceship.itemID, new GCMarsItemRendererSpaceshipTier2());
        MinecraftForgeClient.preloadTexture("/micdoodle8/mods/galacticraft/mars/client/blocks/mars.png");
        MinecraftForgeClient.preloadTexture("/micdoodle8/mods/galacticraft/mars/client/items/mars.png");
    }

    @Override
    public int getGCFluidRenderID()
    {
        return ClientProxyMars.fluidRenderID;
    }

    @Override
    public void spawnParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12, boolean b)
    {
        final Minecraft var14 = FMLClientHandler.instance().getClient();

        if (var14 != null && var14.renderViewEntity != null && var14.effectRenderer != null)
        {
            final double var15 = var14.renderViewEntity.posX - var2;
            final double var17 = var14.renderViewEntity.posY - var4;
            final double var19 = var14.renderViewEntity.posZ - var6;
            Object var21 = null;
            final double var22 = 64.0D;

            if (var15 * var15 + var17 * var17 + var19 * var19 < var22 * var22)
            {
                if (var1.equals("sludgeDrip"))
                {
                    var21 = new GCMarsEntityDropParticleFX(var14.theWorld, var2, var4, var6, GCMarsBlocks.bacterialSludge);
                }
            }

            if (var21 != null)
            {
                ((EntityFX) var21).prevPosX = ((EntityFX) var21).posX;
                ((EntityFX) var21).prevPosY = ((EntityFX) var21).posY;
                ((EntityFX) var21).prevPosZ = ((EntityFX) var21).posZ;
                var14.effectRenderer.addEffect((EntityFX) var21);
            }
        }
    }

    public class ClientPacketHandler implements IPacketHandler
    {
        @Override
        public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player p)
        {
            final DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
            final int packetType = PacketUtil.readPacketID(data);
            if (packetType == 0)
            {

            }
        }
    }

    public static boolean handleBacterialMovement(EntityPlayer player)
    {
        return player.worldObj.isMaterialInBB(player.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), GCMarsBlocks.bacterialSludge);
    }

    public static boolean handleLavaMovement(EntityPlayer player)
    {
        return player.worldObj.isMaterialInBB(player.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.lava);
    }

    public static boolean handleWaterMovement(EntityPlayer player)
    {
        return player.worldObj.isMaterialInBB(player.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.water);
    }

    public static boolean handleLiquidMovement(EntityPlayer player)
    {
        return ClientProxyMars.handleBacterialMovement(player) || ClientProxyMars.handleLavaMovement(player) || ClientProxyMars.handleWaterMovement(player);
    }

    public static class TickHandlerClient implements ITickHandler
    {
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData)
        {
            ClientProxyMars.getCurrentTime = System.currentTimeMillis();

            final Minecraft minecraft = FMLClientHandler.instance().getClient();

            final WorldClient world = minecraft.theWorld;

            final EntityClientPlayerMP player = minecraft.thePlayer;

            if (type.equals(EnumSet.of(TickType.CLIENT)))
            {
                if (player != null && world != null && player.inventory.armorItemInSlot(2) != null && player.inventory.armorItemInSlot(2).getItem().itemID == GCMarsItems.jetpack.itemID && FMLClientHandler.instance().getClient().gameSettings.keyBindJump.pressed && player.posY < 125)
                {
                    ((GCMarsItemJetpack) player.inventory.armorItemInSlot(2).getItem()).setActive();
                    player.motionY -= 0.062D;
                    player.motionY += 0.07 + player.rotationPitch * 2 / 180 * 0.07;
                    player.fallDistance = 0;
                    world.spawnParticle("largesmoke", player.posX, player.posY - 1D, player.posZ, 0, -0.1, 0);
                }

                if (world != null && world.provider instanceof GCMarsWorldProvider)
                {
                    if (world.provider.getSkyRenderer() == null)
                    {
                        world.provider.setSkyRenderer(new GCMarsSkyProvider());
                    }
                }
            }
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
        }

        @Override
        public String getLabel()
        {
            return "Galacticraft Mars Client";
        }

        @Override
        public EnumSet<TickType> ticks()
        {
            return EnumSet.of(TickType.CLIENT);
        }
    }

    @Override
    public String getDimensionName()
    {
        return "Mars";
    }

    @Override
    public String getPlanetSpriteDirectory()
    {
        return "/micdoodle8/mods/galacticraft/mars/client/planets/";
    }

    @Override
    public IPlanetSlotRenderer getSlotRenderer()
    {
        return new GCMarsSlotRenderer();
    }

    @Override
    public IMapPlanet getPlanetForMap()
    {
        return new GCMarsMapPlanet();
    }

    @Override
    public IMapPlanet[] getChildMapPlanets()
    {
        // IMapPlanet[] moonMapPlanet = {new GCCallistoMapPlanet(), new
        // GCEuropaMapPlanet(), new GCIoMapPlanet()};
        // TODO
        return null;
    }

    @Override
    public String getPathToMusicFile()
    {
        return "/micdoodle8/mods/galacticraft/mars/client/sounds/music";
    }
}
