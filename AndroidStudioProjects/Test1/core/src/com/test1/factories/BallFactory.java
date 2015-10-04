package com.test1.factories;


import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class BallFactory {

    public static void addBall(World world) {
        // ball
        BodyDef ballDef = new BodyDef();
        ballDef.type = BodyDef.BodyType.DynamicBody;
        ballDef.position.set(0, 10);

        // ball shape
        CircleShape circleShape = new CircleShape();
        float meters = 2f;
        circleShape.setRadius(meters);

        // fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // 0-1;

        fixtureDef.shape = circleShape;

        world.createBody(ballDef).createFixture(fixtureDef);
        circleShape.dispose();
    }

}// end class