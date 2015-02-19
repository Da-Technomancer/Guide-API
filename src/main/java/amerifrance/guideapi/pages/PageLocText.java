package amerifrance.guideapi.pages;

import amerifrance.guideapi.gui.GuiBase;
import amerifrance.guideapi.objects.Book;
import amerifrance.guideapi.objects.PageBase;
import amerifrance.guideapi.objects.abstraction.CategoryAbstract;
import amerifrance.guideapi.objects.abstraction.EntryAbstract;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;

public class PageLocText extends PageBase {

    public String locText;

    /**
     *
     * @param locText - Pre-localized text to draw.
     */
    public PageLocText(String locText) {
        this.locText = locText;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(Book book, CategoryAbstract category, EntryAbstract entry, int guiLeft, int guiTop, int mouseX, int mouseY, GuiBase guiBase, FontRenderer fontRenderer) {
        fontRenderer.setUnicodeFlag(true);
        fontRenderer.drawSplitString(locText, guiLeft + 37, guiTop + 12, 4 * guiBase.xSize / 6, 0);
        fontRenderer.setUnicodeFlag(false);
    }
}
