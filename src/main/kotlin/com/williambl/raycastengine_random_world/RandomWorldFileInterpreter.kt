package com.williambl.raycastengine_random_world

import com.beust.klaxon.JsonObject
import com.williambl.raycastengine.gameobject.Light
import com.williambl.raycastengine.render.Texture
import com.williambl.raycastengine.world.DefaultWorld
import com.williambl.raycastengine.world.World
import com.williambl.raycastengine.world.WorldFileInterpreter
import kotlin.random.Random

class RandomWorldFileInterpreter: WorldFileInterpreter {

    override fun interpretWorldFile(json: JsonObject): World {
        val mapSize = json.obj("map")
        val mapSizeX = mapSize?.int("x") ?: 0
        val mapSizeY = mapSize?.int("y") ?: 0

        val wallTextures = json.array<String>("wallTextures")!!.map {
            Texture(it)
        }.toTypedArray()

        val world = DefaultWorld(createMap(mapSizeX, mapSizeY, wallTextures.size))

        world.wallTextures = wallTextures

        val worldProperties = json.obj("worldProperties")
        if (worldProperties != null) {
            val floorColor = worldProperties.array<Double>("floorColor")
            val skyColor = worldProperties.array<Double>("skyColor")
            if (floorColor != null)
                world.floorColor = Triple(floorColor[0], floorColor[1], floorColor[2])
            if (skyColor != null)
                world.skyColor = Triple(skyColor[0], skyColor[1], skyColor[2])
        }

        val gameObjects = json.array<JsonObject>("gameObjects")
        if (gameObjects != null) {
            for (gameObjectRepresentation in gameObjects) {
                val gameObject = world.createGameObject(
                    gameObjectRepresentation.string("class")!!,
                    gameObjectRepresentation.int("constructor") ?: 0,
                    *(gameObjectRepresentation.array<Any>("args")!!.toTypedArray())
                )
                if (gameObject == null) {
                    println(gameObjectRepresentation.string("class") + " is not a valid gameObject, skipping")
                    continue
                }

                world.addGameObject(gameObject)
            }
        }

        for (i in 0..20) {
            world.addGameObject(
                Light(
                    Random.nextDouble(mapSizeX.toDouble()),
                    Random.nextDouble(mapSizeY.toDouble()),
                    Random.nextDouble(0.0, 10.0),
                    Random.nextDouble(0.0, 10.0),
                    Random.nextDouble(0.0, 10.0)
                )
            )
        }

        return world
    }

    fun createMap(x: Int, y: Int, wallTypes: Int): Array<IntArray> {
        val map: MutableList<MutableList<Int>> = mutableListOf()
        for (i in 0 until x) {
            val column = mutableListOf<Int>()
            for (j in 0 until y) {
                if (i == 0 || i == x-1 || j == 0 || j == y-1) {
                    column.add(1)
                } else if (i in 1..3 && j in 1..3) {
                    column.add(0)
                } else {
                    if (Random.nextBoolean())
                        column.add(0)
                    else
                        column.add(Random.nextInt(wallTypes))
                }
            }
            map.add(column)
        }

        return map.map { it.toIntArray() }.toTypedArray()
    }

}