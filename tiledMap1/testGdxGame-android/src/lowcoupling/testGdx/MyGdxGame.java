package lowcoupling.testGdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class MyGdxGame extends ApplicationAdapter {
	private OrthographicCamera camera;
	private IsometricTiledMapRenderer mapRenderer;
	private TiledMap map;
	private int camWidth = 700;
	private int camHeight = 100;
	public float zoom = 1.0f;
	public float initialScale = 1.0f;
	private InputMultiplexer inputMultiplexer;
	private MyInputProcessor inputProcessor;
	private MyGestureHandler gestureHandler;
	private Texture textureAnim;
	private Texture texture;
	private MapLayer objectLayer;
	private TextureRegion textureRegion;
	private Vector3 clickCoordinates;
	private TextureMapObject character;
	private int speed = 4;
	private float lastx;
	private float lasty;
	SpriteBatch spriteBatch; //
	// anim
	private static final int FRAME_COLS = 6; // #1
	private static final int FRAME_ROWS = 5;
	private TextureRegion[] walkFrames;
	private Animation walkAnimation;
	private TextureRegion currentFrame;
	float stateTime;

	boolean directionMove = false;
	boolean directionMoveNext = false;
	boolean nearBlue = false;

	boolean directionMoveBank = false;
	boolean directionMoveBankNext = false;
	boolean nearBank = false;

	private Rectangle bankRect;
	private Rectangle buildRect;

	@Override
	public void create() {
		initaliseInputProcessors();
		Texture.setEnforcePotImages(false);
		map = new TmxMapLoader().load("data/isometric_grass_and_water.tmx");
		mapRenderer = new IsometricRenderer(map);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, camWidth * 2, camHeight * 2);
		camera.position.set(camWidth, camHeight, 0);

		// anim frame
		textureAnim = new Texture(Gdx.files.internal("data/wake_shit.png"));
		TextureRegion[][] tmp = TextureRegion.split(textureAnim,
				textureAnim.getWidth() / FRAME_COLS, textureAnim.getHeight()
						/ FRAME_ROWS);
		walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		int index = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				walkFrames[index++] = tmp[i][j];
			}
		}
		walkAnimation = new Animation(0.025f, walkFrames);
		spriteBatch = new SpriteBatch();
		stateTime = 0f;
		// anim

		texture = new Texture(Gdx.files.internal("data/player.png"));
		objectLayer = map.getLayers().get("player");

		TextureRegion playerRegion = new TextureRegion(texture);
		TextureMapObject tmo = new TextureMapObject(playerRegion);
		Vector3 move = new Vector3(690, 170, 0);
		tmo.setX(move.x);
		tmo.setY(move.y);
		objectLayer.getObjects().add(tmo);
		character = (TextureMapObject) map.getLayers().get("player")
				.getObjects().get(0);

		addBuild();
		addBuildBlue();
	}

	private void addBuildBlue() {
		Texture build = new Texture(Gdx.files.internal("data/build_2.png"));
		MapLayer buildLayer = map.getLayers().get("build_2");
		TextureRegion buildRegion = new TextureRegion(build);
		TextureMapObject buildObj = new TextureMapObject(buildRegion);
		Vector3 bankPos = new Vector3(780, -138, 0);
		buildObj.setX(bankPos.x);
		buildObj.setY(bankPos.y);
		buildLayer.getObjects().add(buildObj);
		buildRect = new Rectangle(780, -138, 128, 128);
	}

	private void addBuild() {
		Texture bank = new Texture(Gdx.files.internal("data/build.png"));
		MapLayer bankLayer = map.getLayers().get("build");
		TextureRegion bankRegion = new TextureRegion(bank);
		TextureMapObject bankObj = new TextureMapObject(bankRegion);
		Vector3 bankPos = new Vector3(700, 180, 0);
		bankObj.setX(bankPos.x);
		bankObj.setY(bankPos.y);
		bankLayer.getObjects().add(bankObj);
		bankRect = new Rectangle(700, 180, 128, 128);
	}

	public void initaliseInputProcessors() {

		inputMultiplexer = new InputMultiplexer();

		Gdx.input.setInputProcessor(inputMultiplexer);

		inputProcessor = new MyInputProcessor();
		gestureHandler = new MyGestureHandler();

		inputMultiplexer.addProcessor(new GestureDetector(gestureHandler));
		inputMultiplexer.addProcessor(inputProcessor);
	}

	public static boolean pointInRectangle(Rectangle r, float x, float y) {
		return r.x <= x && r.x + r.width >= x && r.y <= y
				&& r.y + r.height >= y;
	}

	@Override
	public void dispose() {
		mapRenderer.dispose();
		map.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 15);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		mapRenderer.setView(camera);
		camera.zoom = zoom;
		camera.update();
		mapRenderer.render();

		// stateTime += Gdx.graphics.getDeltaTime();
		// currentFrame = walkAnimation.getKeyFrame(stateTime, true);
		// spriteBatch.begin();
		// spriteBatch.draw(currentFrame, 50, 50);
		// spriteBatch.end();
		if (directionMove)
			clickMoveToBuild();
		if (directionMoveNext)
			clickMoveToBuildNext();
		if (directionMoveBank)
			clickMoveToBank();
		if (directionMoveBankNext)
			clickMoveToBankNext();
	}

	private void clickMoveToBuild() {

		if (character.getX() > 500) {
			character.setX((float) character.getX() - 3);
			character.setY((float) character.getY() - 2);
		} else {
			directionMove = false;
			directionMoveNext = true;
		}
	}

	private void clickMoveToBuildNext() {

		if (character.getY() > -100) {
			character.setX((float) character.getX() + 4);
			character.setY((float) character.getY() - 2);
		} else {
			directionMoveNext = false;
			nearBlue = true;
		}
	}

	private void clickMoveToBank() {

		if (character.getX() < 900) {
			character.setX((float) character.getX() + 3);
			character.setY((float) character.getY() + 2);
		} else {
			directionMoveBank = false;
			directionMoveBankNext = true;
		}
	}

	private void clickMoveToBankNext() {

		if (character.getY() < 170) {
			character.setX((float) character.getX() - 2);
			character.setY((float) character.getY() + 2);
		} else {
			directionMoveBankNext = false;
			nearBlue = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = height;
		camera.viewportWidth = width;
		camera.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	public class MyInputProcessor implements InputProcessor {

		@Override
		public boolean scrolled(int amount) {

			// Zoom out
			if (amount > 0 && zoom < 1) {
				zoom += 0.1f;
			}

			// Zoom in
			if (amount < 0 && zoom > 0.1) {
				zoom -= 0.1f;
			}

			return true;
		}

		@Override
		public boolean keyDown(int keycode) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {

			clickCoordinates = new Vector3(screenX, screenY, 0);

			camera.unproject(clickCoordinates);

			Logger.e("TAG", clickCoordinates.x + "," + clickCoordinates.y
					+ ".." + character.getX() + "," + character.getY());

			if (pointInRectangle(bankRect, clickCoordinates.x,
					clickCoordinates.y) && nearBlue) {
				Logger.e("TAG", "Bank");
				directionMoveBank = true;
			} else if (pointInRectangle(buildRect, clickCoordinates.x,
					clickCoordinates.y) && !nearBlue) {
				Logger.e("TAG", "build");
				directionMove = true;
			}
			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {

			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class MyGestureHandler implements GestureListener {

		public float initialScale = 1.0f;

		@Override
		public boolean touchDown(float x, float y, int pointer, int button) {

			initialScale = zoom;

			return false;
		}

		@Override
		public boolean zoom(float initialDistance, float distance) {

			// Calculate pinch to zoom
			float ratio = initialDistance / distance;

			// Clamp range and set zoom
			zoom = MathUtils.clamp(initialScale * ratio, 0.1f, 1.0f);

			return true;
		}

		@Override
		public boolean tap(float x, float y, int count, int button) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean longPress(float x, float y) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean fling(float velocityX, float velocityY, int button) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean pan(float x, float y, float deltaX, float deltaY) {

			camera.position.add(camera.zoom * -deltaX, camera.zoom * deltaY, 0);

			return true;
		}

		@Override
		public boolean panStop(float x, float y, int pointer, int button) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
				Vector2 pointer1, Vector2 pointer2) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
