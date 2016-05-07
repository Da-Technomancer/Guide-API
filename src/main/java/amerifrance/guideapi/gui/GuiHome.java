package amerifrance.guideapi.gui;

import amerifrance.guideapi.api.impl.Book;
import amerifrance.guideapi.api.impl.abstraction.CategoryAbstract;
import amerifrance.guideapi.button.ButtonNext;
import amerifrance.guideapi.button.ButtonPrev;
import amerifrance.guideapi.network.PacketHandler;
import amerifrance.guideapi.network.PacketSyncHome;
import amerifrance.guideapi.wrapper.CategoryWrapper;
import com.google.common.collect.HashMultimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiHome extends GuiBase {

    public ResourceLocation outlineTexture;
    public ResourceLocation pageTexture;
    public Book book;
    public HashMultimap<Integer, CategoryWrapper> categoryWrapperMap = HashMultimap.create();
    public ButtonNext buttonNext;
    public ButtonPrev buttonPrev;
    public int categoryPage;

    public GuiHome(Book book, EntityPlayer player, ItemStack bookStack) {
        super(player, bookStack);
        this.book = book;
        this.pageTexture = book.getPageTexture();
        this.outlineTexture = book.getOutlineTexture();
        this.categoryPage = 0;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.categoryWrapperMap.clear();

        guiLeft = (this.width - this.xSize) / 2;
        guiTop = (this.height - this.ySize) / 2;

        this.buttonList.add(buttonNext = new ButtonNext(0, guiLeft + 4 * xSize / 6, guiTop + 5 * ySize / 6, this));
        this.buttonList.add(buttonPrev = new ButtonPrev(1, guiLeft + xSize / 5, guiTop + 5 * ySize / 6, this));

        int cX = guiLeft;
        int cY = guiTop + 15;
        boolean drawOnLeft = true;
        int i = 0;
        int pageNumber = 0;

        for (CategoryAbstract category : book.getCategoryList()) {
            category.onInit(book, this, player, bookStack);
            if (drawOnLeft) {
                categoryWrapperMap.put(pageNumber, new CategoryWrapper(book, category, cX, cY, 15, 15, player, this.fontRendererObj, this.itemRender, true, bookStack));
                cX = guiLeft + 180;
                drawOnLeft = false;
            } else {
                categoryWrapperMap.put(pageNumber, new CategoryWrapper(book, category, cX, cY, 15, 15, player, this.fontRendererObj, this.itemRender, false, bookStack));
                cY += 25;
                cX = guiLeft;
                drawOnLeft = true;
            }
            i++;

            if (i >= 12) {
                i = 0;
                cX = guiLeft;
                cY = guiTop + 15;
                pageNumber++;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
        for (CategoryWrapper wrapper : this.categoryWrapperMap.get(categoryPage))
            if (wrapper.canPlayerSee())
                wrapper.draw(mouseX, mouseY, this);

        Minecraft.getMinecraft().getTextureManager().bindTexture(pageTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        Minecraft.getMinecraft().getTextureManager().bindTexture(outlineTexture);
        drawTexturedModalRectWithColor(guiLeft, guiTop, 0, 0, xSize, ySize, book.getColor());
        drawSplitString(book.getLocalizedWelcomeMessage(), guiLeft + 37, guiTop + 12, (4 * xSize / 6) - 4, 0);

        for (CategoryWrapper wrapper : this.categoryWrapperMap.get(categoryPage))
            if (wrapper.canPlayerSee())
                wrapper.drawExtras(mouseX, mouseY, this);

        drawCenteredString(fontRendererObj, String.valueOf(categoryPage + 1) + "/" + String.valueOf(categoryWrapperMap.asMap().size()), guiLeft + xSize / 2, guiTop + 5 * ySize / 6, 0);
        drawCenteredStringWithShadow(fontRendererObj, book.getLocalizedBookTitle(), guiLeft + xSize / 2, guiTop - 10, Color.WHITE.getRGB());

        buttonPrev.visible = categoryPage != 0;
        buttonNext.visible = categoryPage != categoryWrapperMap.asMap().size() - 1;

        super.drawScreen(mouseX, mouseY, renderPartialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int typeofClick) {
        try {
            super.mouseClicked(mouseX, mouseY, typeofClick);

            for (CategoryWrapper wrapper : this.categoryWrapperMap.get(categoryPage)) {
                if (wrapper.isMouseOnWrapper(mouseX, mouseY) && wrapper.canPlayerSee()) {
                    if (typeofClick == 0)
                        wrapper.category.onLeftClicked(book, mouseX, mouseY, player, bookStack);

                    else if (typeofClick == 1)
                        wrapper.category.onRightClicked(book, mouseX, mouseY, player, bookStack);
                }
            }
        } catch (IOException e) {
            // Pokeball! Go!
        }
    }

    @Override
    public void handleMouseInput() {
        try {
            super.handleMouseInput();

            int movement = Mouse.getEventDWheel();
            if (movement < 0)
                nextPage();
            else if (movement > 0)
                prevPage();
        } catch (IOException e) {
            // Pokeball! Go!
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        if ((keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_RIGHT) && categoryPage + 1 < categoryWrapperMap.asMap().size())
            nextPage();

        if ((keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_LEFT) && categoryPage > 0)
            prevPage();
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0 && categoryPage + 1 < categoryWrapperMap.asMap().size())
            nextPage();
        else if (button.id == 1 && categoryPage > 0)
            prevPage();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        PacketHandler.INSTANCE.sendToServer(new PacketSyncHome(categoryPage));
    }

    public void nextPage() {
        if (categoryPage != categoryWrapperMap.asMap().size() - 1)
            categoryPage++;
    }

    public void prevPage() {
        if (categoryPage != 0)
            categoryPage--;
    }
}
