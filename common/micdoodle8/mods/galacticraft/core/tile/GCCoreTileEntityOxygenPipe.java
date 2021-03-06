package micdoodle8.mods.galacticraft.core.tile;

import mekanism.api.EnumGas;
import mekanism.api.IPressurizedTube;
import mekanism.api.ITubeConnection;
import micdoodle8.mods.galacticraft.API.IColorable;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import com.google.common.io.ByteArrayDataInput;

public class GCCoreTileEntityOxygenPipe extends TileEntity implements ITubeConnection, IPressurizedTube, IColorable, IPacketReceiver
{
    private byte pipeColor = 15;
    private byte preLoadColor;
    private byte preColorCooldown;
    private boolean setColor = false;

    @Override
    public boolean canTransferGas(TileEntity fromTile)
    {
        if (fromTile instanceof IColorable)
        {
            if (this.getColor() == ((IColorable) fromTile).getColor())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean canUpdate()
    {
        return !this.setColor;
    }

    @Override
    public void updateEntity()
    {
        if (this.preColorCooldown > 0)
        {
            this.preColorCooldown--;
        }

        if (this.preColorCooldown == 0 && !this.worldObj.isRemote && this.preLoadColor != -1)
        {
            PacketManager.sendPacketToClients(PacketManager.getPacket(GalacticraftCore.CHANNELENTITIES, this, this.getColor(), this.preLoadColor));
            this.preLoadColor = -1;
            this.setColor = true;
        }

        if (this.preColorCooldown == 0 && this.worldObj.isRemote && this.preLoadColor == 0)
        {
            final Vector3 thisVec = new Vector3(this);
            this.worldObj.markBlockForRenderUpdate(thisVec.intX(), thisVec.intY(), thisVec.intZ());
            this.preLoadColor = -1;
            this.setColor = true;
        }
    }

    @Override
    public void validate()
    {
        super.validate();

        this.preColorCooldown = 40;

        if (this.worldObj != null && this.worldObj.isRemote)
        {
            final Vector3 thisVec = new Vector3(this);
            this.worldObj.markBlockForRenderUpdate(thisVec.intX(), thisVec.intY(), thisVec.intZ());
        }
    }

    @Override
    public void setColor(byte col)
    {
        this.pipeColor = col;

        if (this.worldObj != null && this.worldObj.isRemote)
        {
            final Vector3 thisVec = new Vector3(this);
            this.worldObj.markBlockForRenderUpdate(thisVec.intX(), thisVec.intY(), thisVec.intZ());
        }
    }

    @Override
    public byte getColor()
    {
        return this.pipeColor;
    }

    @Override
    public void onAdjacentColorChanged(Vector3 thisVec, Vector3 updatedVec)
    {
        this.worldObj.markBlockForUpdate(thisVec.intX(), thisVec.intY(), thisVec.intZ());
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        final byte by = par1NBTTagCompound.getByte("pipeColor");
        this.setColor(by);
        this.preLoadColor = by;
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);

        par1NBTTagCompound.setByte("pipeColor", this.getColor());
    }

    @Override
    public boolean canTubeConnect(ForgeDirection side)
    {
        final TileEntity tile = this.worldObj.getBlockTileEntity(this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ);

        if (tile != null && tile instanceof IColorable)
        {
            final byte color = ((IColorable) tile).getColor();

            if (color == this.getColor())
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
    {
        try
        {
            if (this.worldObj.isRemote)
            {
                this.setColor(dataStream.readByte());
                this.preLoadColor = dataStream.readByte();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onTransfer(EnumGas type)
    {
        ;
    }
}
