package net.flintmc.mcapi.internal.resources;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.flintmc.mcapi.internal.resources.SkinCacheWebResource;
import net.flintmc.mcapi.internal.resources.SkinCacheWebResource.Factory;
import net.flintmc.mcapi.player.gameprofile.GameProfile;
import net.flintmc.mcapi.player.skin.SkinProvider;
import net.flintmc.mcapi.resources.ResourceLocation;
import net.flintmc.mcapi.resources.ResourceLocationProvider;
import net.flintmc.mcapi.resources.ResourceLocationWatcher;
import net.flintmc.render.gui.webgui.WebFileSystem;
import net.flintmc.render.gui.webgui.WebFileSystemHandler;
import net.flintmc.render.gui.webgui.WebResource;

@Singleton
@WebFileSystem("minecraft-skin-cache-head")
public class MinecraftHeadSkinCacheWebFileSystem implements WebFileSystemHandler {

  private final ResourceLocationProvider resourceLocationProvider;
  private final MinecraftSkinCacheDirectoryProvider minecraftSkinCacheDirectoryProvider;
  private final SkinCacheWebResource.Factory skinCacheWebResourceFactory;

  @Inject
  private MinecraftHeadSkinCacheWebFileSystem(
      ResourceLocationProvider resourceLocationProvider,
      MinecraftSkinCacheDirectoryProvider minecraftSkinCacheDirectoryProvider,
      Factory skinCacheWebResourceFactory) {
    this.resourceLocationProvider = resourceLocationProvider;
    this.minecraftSkinCacheDirectoryProvider = minecraftSkinCacheDirectoryProvider;
    this.skinCacheWebResourceFactory = skinCacheWebResourceFactory;
  }

  @Override
  public boolean existsFile(String path) {
    return new File(this.minecraftSkinCacheDirectoryProvider.getSkinCacheDirectory(), path)
        .exists();
  }

  @Override
  public WebResource getFile(String path) throws FileNotFoundException {
    try {

      ResourceLocation resourceLocation = this.resourceLocationProvider.get(path);
      if (resourceLocation.exists()) {
        try (InputStream inputStream = resourceLocation.openInputStream()) {
          BufferedImage bufferedImage = ImageIO.read(inputStream);

          File file = new File(minecraftSkinCacheDirectoryProvider.getSkinCacheDirectory(),
              "heads/" + path);
          writeHead(bufferedImage, file);

          return new ResourcePackWebResource(resourceLocationProvider.get("skins/heads/" + path));
        }
      }
      path = path.replaceFirst("skins/", "");
      path = path.substring(0, 2) + "/" + path;
      BufferedImage bufferedImage = ImageIO
          .read(new File(this.minecraftSkinCacheDirectoryProvider.getSkinCacheDirectory(), path));
      path = "heads/" + path;
      File file = new File(this.minecraftSkinCacheDirectoryProvider.getSkinCacheDirectory(), path);
      writeHead(bufferedImage, file);

      return skinCacheWebResourceFactory
          .create(minecraftSkinCacheDirectoryProvider.getSkinCacheDirectory(), path);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void writeHead(BufferedImage bufferedImage, File file) throws IOException {
    file.getParentFile().mkdirs();

    BufferedImage bi = new BufferedImage(32 * bufferedImage.getWidth(null),
        32 * bufferedImage.getHeight(null),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D grph = (Graphics2D) bi.getGraphics();
    grph.scale(32, 32);

    // everything drawn with grph from now on will get scaled.

    grph.drawImage(bufferedImage, 0, 0, null);
    grph.dispose();

    if (bi.getWidth() / bi.getHeight() == 1) {
      BufferedImage firstLayer = bi
          .getSubimage(bi.getWidth() / 8, bi.getHeight() / 8,
              bi.getWidth() / 8, bi.getHeight() / 8);

      BufferedImage secondLayer = bi
          .getSubimage(5 * bi.getWidth() / 8, bi.getHeight() / 8,
              bi.getWidth() / 8, bi.getHeight() / 8);

      Graphics2D graphics = (Graphics2D) firstLayer.getGraphics();
      graphics.drawImage(secondLayer, 0, 0, null);

      ImageIO.write(firstLayer, "PNG", file);
    } else if (bi.getWidth() / bi.getHeight() == 2) {
      BufferedImage firstLayer = bi
          .getSubimage(bi.getWidth() / 8, bi.getHeight() / 4,
              bi.getWidth() / 8, bi.getHeight() / 4);

      BufferedImage secondLayer = bi
          .getSubimage(5 * bi.getWidth() / 8, bi.getHeight() / 4,
              bi.getWidth() / 8, bi.getHeight() / 4);

      Graphics2D graphics = (Graphics2D) firstLayer.getGraphics();
      graphics.drawImage(secondLayer, 0, 0, null);

      ImageIO.write(firstLayer, "PNG", file);
    } else {
      throw new IllegalStateException("Malformed texture");
    }
  }
}
