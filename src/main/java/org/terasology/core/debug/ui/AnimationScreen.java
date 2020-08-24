/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.debug.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UISlider;

import java.util.ArrayList;

/**
 * A Tool for testing the animations of entities.
 * <p>
 * Animation speed can be edited with this utility
 */
public class AnimationScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(AnimationScreen.class);
    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    private UIButton spawnEntityIdButton;
    private UISlider animationSpeedSlider;
    private UIDropdownScrollable<ResourceUrn> entityDropdown;

    @Override
    public void initialise() {
        spawnEntityIdButton = find("spawnEntityIdButton", UIButton.class);
        entityDropdown = find("entityDropdown", UIDropdownScrollable.class);
        ArrayList skeletalMesh = new ArrayList(assetManager.getAvailableAssets(SkeletalMesh.class));
        if (entityDropdown != null) {
            entityDropdown.setOptions(skeletalMesh);
        }
        animationSpeedSlider = find("entityAnimationSpeedSlider", UISlider.class);
        if (animationSpeedSlider != null) {
            animationSpeedSlider.setMinimum(-0.0f);
            animationSpeedSlider.setIncrement(0.1f);
            animationSpeedSlider.setRange(10.0f);
            animationSpeedSlider.setPrecision(1);
        }
        spawnEntityIdButton.subscribe(widget -> {
            Vector3f localPlayerPosition = localPlayer.getPosition();
            Vector3f offset = localPlayer.getViewDirection();
            offset.scale(2.0f);
            offset.y = 0;
            localPlayerPosition.add(offset);

            Quat4f localPlayerRotation = localPlayer.getRotation();

            assetManager.getAsset(entityDropdown.getSelection(), Prefab.class)
                    .filter(prefab -> prefab.hasComponent(LocationComponent.class))
                    .filter(prefab -> prefab.hasComponent(SkeletalMeshComponent.class))
                    .ifPresent(prefab -> {
                        EntityRef entity = entityManager.create(prefab, localPlayerPosition, localPlayerRotation);

                        // set animation rate according to user choice
                        SkeletalMeshComponent skeletalMeshComponent = entity.getComponent(SkeletalMeshComponent.class);
                        skeletalMeshComponent.animationRate = animationSpeedSlider.getValue();
                        entity.saveComponent(skeletalMeshComponent);

                        // adjust speed multiplier of character entities
                        if (entity.hasComponent(CharacterMovementComponent.class)) {
                            CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
                            movementComponent.speedMultiplier = animationSpeedSlider.getValue();
                            entity.saveComponent(movementComponent);
                        }
                    });
        });

    }
}

