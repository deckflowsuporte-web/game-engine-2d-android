package com.deckflow.engine;

import java.io.*;

/**
 * RigidBody2D - Physics body with velocity, forces, gravity
 */
public class RigidBody2D extends Node {
    private static final long serialVersionUID = 1L;
    
    public BodyType bodyType = BodyType.STATIC;
    public float mass = 1;
    public float friction = 0.3f;
    public float bounciness = 0.3f;
    public boolean useGravity = true;
    public float gravityScale = 1;
    
    public Vector2 velocity = new Vector2();
    public Vector2 acceleration = new Vector2();
    public Vector2 appliedForce = new Vector2();
    
    public float angularVelocity = 0;
    public float torque = 0;
    public float momentOfInertia = 1;
    
    public enum BodyType {
        STATIC,
        KINEMATIC,
        DYNAMIC
    }
    
    @Override
    public void _physicsProcess(float delta) {
        super._physicsProcess(delta);
        
        if (bodyType == BodyType.DYNAMIC) {
            // Apply gravity
            if (useGravity) {
                appliedForce.y += 980 * mass * gravityScale;
            }
            
            // Apply forces
            acceleration.x = appliedForce.x / mass;
            acceleration.y = appliedForce.y / mass;
            
            // Update velocity
            velocity.x += acceleration.x * delta;
            velocity.y += acceleration.y * delta;
            
            // Apply friction
            velocity.x *= (1 - friction * delta);
            
            // Update position
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            
            // Reset forces
            appliedForce = new Vector2();
        }
    }
    
    public void applyForce(Vector2 force) {
        appliedForce = appliedForce.add(force);
    }
    
    public void applyImpulse(Vector2 impulse) {
        velocity = velocity.add(impulse.mul(1/mass));
    }
    
    public void setLinearVelocity(Vector2 vel) {
        velocity = vel;
    }
    
    public void setAngularVelocity(float av) {
        angularVelocity = av;
    }
}
