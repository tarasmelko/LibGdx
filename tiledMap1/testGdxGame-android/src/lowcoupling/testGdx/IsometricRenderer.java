package lowcoupling.testGdx;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;

public class IsometricRenderer extends IsometricTiledMapRenderer {

	public IsometricRenderer(TiledMap map) {
		super(map);
	}

	@Override
	public void renderObject(MapObject object) {
		if (object instanceof TextureMapObject) {
			TextureMapObject textureObj = (TextureMapObject) object;
			spriteBatch.draw(textureObj.getTextureRegion(), textureObj.getX(),
					textureObj.getY());
		}
	}

}
