package amerifrance.guideapi.util;

import amerifrance.guideapi.ConfigHandler;
import amerifrance.guideapi.ModInformation;
import amerifrance.guideapi.api.GuideRegistry;
import amerifrance.guideapi.api.base.Book;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            NBTTagCompound tag = getModTag(player, ModInformation.ID);
            if (ConfigHandler.canSpawnWithBooks) {
                for (Book book : GuideRegistry.getBookList()) {
                    if (book.isSpawnWithBook() && !tag.getBoolean("hasInitial" + book.getUnlocBookTitle())) {
                        player.inventory.addItemStackToInventory(GuideRegistry.getItemStackForBook(book));
                        player.inventoryContainer.detectAndSendChanges();
                        tag.setBoolean("hasInitial" + book.getUnlocBookTitle(), true);
                    }
                }
            }
        }
    }

    public NBTTagCompound getModTag(EntityPlayer player, String modName) {
        NBTTagCompound tag = player.getEntityData();
        NBTTagCompound persistTag;
        if (tag.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
            persistTag = tag.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        else {
            persistTag = new NBTTagCompound();
            tag.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistTag);
        }
        NBTTagCompound modTag;
        if (persistTag.hasKey(modName)) {
            modTag = persistTag.getCompoundTag(modName);
        } else {
            modTag = new NBTTagCompound();
            persistTag.setTag(modName, modTag);
        }
        return modTag;
    }
}
