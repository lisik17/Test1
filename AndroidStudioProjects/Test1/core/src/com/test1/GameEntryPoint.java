package com.test1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;



public class GameEntryPoint extends ApplicationAdapter {

	private World world;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera camera;

	//TODO Alisa
	//string constants for user data
	private static final String BASKET = "basket";
	private static final String GROUND = "ground";
	private static final String WALL = "wall";

	private Body ball;
	private boolean setInitBallPos;
	private BodyFactory bodyFactory;
	private MouseJointDef jointDef;
	private MouseJoint joint;
	private static int score, lives;
	private int ballInit_x = -7, ballInit_y = 3, basketInit_x =7, basketInit_y =8;
	private Text text;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITY_ITERATIONS = 6;
	private final int POSITION_ITERATIONS = 2;


	@Override
	public void create() {
		Box2D.init();
		float gravityX = 0f;
		float gravityY = -9.81f; // -9.81f
		boolean allowSleep = true;
		world = new World(new Vector2(gravityX, gravityY), allowSleep);
		debugRenderer = new Box2DDebugRenderer();
		camera = new OrthographicCamera();


		//Alisa

		//when the ball hits the basket we need to move it to init pos
		setInitBallPos = false;

		bodyFactory = new BodyFactory();

		//init ball
		ball = bodyFactory.addBall();

		//init walls
		bodyFactory.addWall(world, 9, -500, 9, 500,WALL);
		bodyFactory.addWall(world, -9, -500, -9, 500,WALL);
		bodyFactory.addWall(world, -500, 14, 500, 14, WALL);
		bodyFactory.addWall(world, -500, -3, 500, -3, GROUND);

		//init basket
		bodyFactory.addBasket(1, 1);

		//init score
		score = 0;
		lives = 3;
		//bodyDef.position.set(8, 6);


		//init text
		text = new Text();

		//init listeners
		Gdx.input.setInputProcessor(new MyInputListener());
		world.setContactListener(new MyContactListener());

		// mouse joint
		jointDef = new MouseJointDef();
		jointDef.bodyA = world.createBody(new BodyDef());
		jointDef.collideConnected = true;
		jointDef.maxForce = 500;

	}

	@Override
	public void render() {
		setBackground();
		renderWorld();
	}

	private void setBackground() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	private void renderWorld() {
		debugRenderer.render(world, camera.combined);
		world.step(TIMESTEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

		if(setInitBallPos){
			ball.setTransform(ballInit_x,ballInit_y,ball.getAngle());
			setInitBallPos = false;
		}
		if(lives <= 0){
			ball.setTransform(ballInit_x,ballInit_y,ball.getAngle());
			text.printText("game over" + "\n" + "your score is : "+score);
		}else if(lives > 0){
			text.printText("your score is : " + score +"\n"+ "number of lives : "+lives);
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		int zoom = 25;
		camera.viewportWidth = width / zoom;
		camera.viewportHeight = height / zoom;
		camera.update();
	}

	@Override
	public void dispose() {
		world.dispose();
		debugRenderer.dispose();

		//free text objects
		if(null != text){
			text.textDisposure();
		}

		ball = null;


	}


	////////////////////////////////////////////////////////////////////////////////////////////////////
	//Alisa
	class MyInputListener implements InputProcessor {
		@Override
		public boolean keyDown(int keycode) {
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

		private Vector3 tmp = new Vector3();
		private Vector2 tmp2 = new Vector2();

		private QueryCallback queryCallback = new QueryCallback() {

			//is called if we find a fixture in rectangle AABB
			@Override
			public boolean reportFixture(Fixture fixture) {
				//if point is not inside the fixture
				if(!fixture.testPoint(tmp.x,tmp.y)){
					return false;
				}

				jointDef.bodyB = fixture.getBody();
				//the target where we wont to move the body to (mouse position)
				jointDef.target.set(tmp.x,tmp.y);
				joint = (MouseJoint)world.createJoint(jointDef);


				//ball.setActive(true);

				return false;
			}
		};

		//
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
								 int button) {
			//translate point in screen to coordinates
			camera.unproject(tmp.set(screenX,screenY,0));

			//if there is a fixture inside the rectangle give me that
			world.QueryAABB(queryCallback, tmp.x, tmp.y, tmp.x, tmp.y);

			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			//no object
			if(joint == null){
				return false;
			}

			if(!world.isLocked()) {
				world.destroyJoint(joint);
				joint = null;
			}

			return true;
		}

		//
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			//no object to drag
			if(joint == null){
				return false;
			}

			camera.unproject((tmp.set(screenX,screenY,0)));
			//update target
			joint.setTarget(tmp2.set(tmp.x,tmp.y));
			return true;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			// TODO Auto-generated method stub
			return false;
		}

		//
		@Override
		public boolean scrolled(int amount) {
			return true;
		}
	}// end cl

	public class MyContactListener implements ContactListener{



		public  MyContactListener(){
		}

		private void onEndContact(Contact contact){
			contact.setEnabled(false);
			ball.setAwake(false);
			setInitBallPos = true;
		}

		@Override
		public void endContact(Contact contact) {
			if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals(BASKET)) {
				onEndContact(contact);
				score += 5;
			}
			if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals(BASKET)) {
				onEndContact(contact);
				score += 5;
			}
			if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals(GROUND)) {
				onEndContact(contact);
				lives--;
			}
			if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals(GROUND)) {
				onEndContact(contact);
				lives--;
			}
		}

		@Override
		public void beginContact(Contact contact) {
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold){
			WorldManifold manifold = contact.getWorldManifold();
			for(int j = 0; j < manifold.getNumberOfContactPoints(); j++){

				if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals(BASKET)) {
					contact.setEnabled(false);
				}
				if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals(BASKET)) {
					contact.setEnabled(false);
				}
			}
		}

		@Override
		public void postSolve (Contact contact, ContactImpulse impulse){
		}
	}

	public class Text{

		private SpriteBatch batch;
		private BitmapFont font;

		public void printText(String str){
			batch = new SpriteBatch();
			font = new BitmapFont();
			font.setColor(Color.GREEN);
			font.getData().scale(2);

			batch.begin();
			font.draw(batch, str, 90, 300);
			batch.end();
		}

		//dispose batch, font
		public void textDisposure(){
			batch.dispose();
			font.dispose();
		}
	}

	public class BodyFactory{
		private Body body;

		public Body addBall() {
			// ball
			BodyDef ballDef = new BodyDef();
			ballDef.type = BodyDef.BodyType.DynamicBody;
			ballDef.position.set(ballInit_x, ballInit_y);

			// ball shape
			CircleShape circleShape = new CircleShape();
			float meters = 1f;
			circleShape.setRadius(meters);

			// fixture
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.density = 0.5f;
			fixtureDef.friction = 0.4f;
			fixtureDef.restitution = 0.6f; // 0-1;
			fixtureDef.shape = circleShape;


			body = world.createBody(ballDef);
			body.createFixture(fixtureDef);
			circleShape.dispose();

			body.setAwake(false);

			return body;
		}

		public Body addWall(World world,int x1, int y1, int x2, int y2, String userData){
			// wall
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.StaticBody;
			bodyDef.position.set(0, 0);

			// ground shape
			ChainShape wallShape = new ChainShape();
			wallShape.createChain(new Vector2[]{new Vector2(x1, y1), new Vector2(x2, y2)});

			// fixture
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.density = 2.5f;
			fixtureDef.friction = 0.5f;
			fixtureDef.restitution = 0f; // 0-1;
			fixtureDef.shape = wallShape;

			body = world.createBody(bodyDef);
			body.createFixture(fixtureDef).setUserData(userData);
			wallShape.dispose();

			return body;
		}

		public Body addBasket(int x, int y){

			BodyDef bodyDef = new BodyDef();
			bodyDef.type =  BodyDef.BodyType.StaticBody;
			bodyDef.position.set(basketInit_x, basketInit_y);

			PolygonShape polyShape = new PolygonShape();
			polyShape.setAsBox(x, y);

			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.density = 2.5f;
			fixtureDef.density = 2.5f;
			fixtureDef.friction = 0.5f;
			fixtureDef.restitution = 1f; // 0-1;
			fixtureDef.shape = polyShape;

			body = world.createBody(bodyDef);
			body.createFixture(fixtureDef).setUserData(BASKET);
			polyShape.dispose();

			return body;
		}


	}

////////////////////////////////////////////////////////////////////////////////////////////////////
}