package com.example.game.levels

import com.example.game.model.*

object LevelRepository {

    fun getLevel(levelId: Int): GameLevel {
        return when (levelId) {
            1 -> createLevel1Spring()
            2 -> createLevel2Summer()
            3 -> createLevel3Autumn()
            4 -> createLevel4WinterBoss()
            else -> createLevel1Spring()
        }
    }

    val totalLevels = 4

    /**
     * Nivel 1: Primavera en el Corral
     * Introducción a mecánicas: saltos, manzanas arrojables,
     * enemigos chibi con cuchillos y pisar enemigos para marearlos.
     */
    private fun createLevel1Spring(): GameLevel {
        val platforms = mutableListOf<Platform>()
        val enemies = mutableListOf<Enemy>()
        val collectibles = mutableListOf<Collectible>()

        // Suelo principal continuo con desniveles
        platforms.add(Platform("g1", 0f, 680f, 900f, 120f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("g2", 980f, 680f, 1200f, 120f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("g3", 2260f, 680f, 1200f, 120f, PlatformType.SOLID_GROUND))

        // Vallas y plataformas elevadas de madera
        platforms.add(Platform("p1", 320f, 540f, 140f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p2", 520f, 440f, 160f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p3", 750f, 350f, 180f, 26f, PlatformType.WOOD_PLANK))

        // Fardo de heno saltarín sobre el foso
        platforms.add(Platform("hay1", 910f, 660f, 60f, 30f, PlatformType.BOUNCY_HAY))

        // Corral y zona media
        platforms.add(Platform("p4", 1150f, 550f, 200f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p5", 1450f, 460f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p6", 1720f, 560f, 160f, 26f, PlatformType.WOOD_PLANK))

        // Cajas rompibles con Salto Bomba
        platforms.add(Platform("crate1", 1320f, 630f, 50f, 50f, PlatformType.BREAKABLE_CRATE))
        platforms.add(Platform("crate2", 1370f, 630f, 50f, 50f, PlatformType.BREAKABLE_CRATE))

        // Zona final hacia el portón del corral
        platforms.add(Platform("p7", 2400f, 550f, 150f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p8", 2620f, 440f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p9", 2860f, 560f, 200f, 26f, PlatformType.WOOD_PLANK))

        // Enemigos: Niños chibis con cuchillos y gallos
        enemies.add(
            Enemy(
                id = "e1_chibi1",
                type = EnemyType.CHIBI_KNIFE,
                x = 600f,
                y = 636f,
                patrolMinX = 400f,
                patrolMaxX = 850f,
                baseSpeed = 95f
            )
        )
        enemies.add(
            Enemy(
                id = "e1_rooster1",
                type = EnemyType.ROOSTER,
                x = 1200f,
                y = 636f,
                patrolMinX = 1100f,
                patrolMaxX = 1300f,
                baseSpeed = 120f
            )
        )
        enemies.add(
            Enemy(
                id = "e1_chibi2",
                type = EnemyType.CHIBI_KNIFE,
                x = 1550f,
                y = 636f,
                patrolMinX = 1420f,
                patrolMaxX = 1800f,
                baseSpeed = 110f
            )
        )
        enemies.add(
            Enemy(
                id = "e1_chibi3",
                type = EnemyType.CHIBI_FORK,
                x = 1750f,
                y = 516f,
                patrolMinX = 1720f,
                patrolMaxX = 1870f,
                baseSpeed = 85f
            )
        )
        enemies.add(
            Enemy(
                id = "e1_chibi4",
                type = EnemyType.CHIBI_KNIFE,
                x = 2500f,
                y = 636f,
                patrolMinX = 2350f,
                patrolMaxX = 2750f,
                baseSpeed = 125f
            )
        )

        // Coleccionables: Manzanas arrojables, monedas y bellotas
        collectibles.add(Collectible("c1", CollectibleType.APPLE, 220f, 620f))
        collectibles.add(Collectible("c2", CollectibleType.APPLE, 270f, 620f))
        collectibles.add(Collectible("c3", CollectibleType.GOLD_COIN, 380f, 490f))
        collectibles.add(Collectible("c4", CollectibleType.APPLE, 580f, 390f))
        collectibles.add(Collectible("c5", CollectibleType.ACORN, 820f, 300f))
        collectibles.add(Collectible("c6", CollectibleType.HEART, 1345f, 570f))
        collectibles.add(Collectible("c7", CollectibleType.APPLE, 1490f, 410f))
        collectibles.add(Collectible("c8", CollectibleType.GOLD_COIN, 1780f, 510f))
        collectibles.add(Collectible("c9", CollectibleType.APPLE, 2050f, 620f))
        collectibles.add(Collectible("c10", CollectibleType.GOLD_COIN, 2450f, 500f))
        collectibles.add(Collectible("c11", CollectibleType.ACORN, 2700f, 390f))

        return GameLevel(
            id = 1,
            season = Season.SPRING,
            title = "Nivel 1: Escape del Corral",
            subtitle = "¡Huye antes de que te atrapen los granjeros!",
            worldWidth = 3200f,
            worldHeight = 900f,
            playerStartX = 80f,
            playerStartY = 600f,
            exitX = 3080f,
            exitY = 600f,
            platforms = platforms,
            enemies = enemies,
            collectibles = collectibles,
            hints = listOf(
                Pair(120f, "¡Salta sobre los niños para marearlos con pajaritos!"),
                Pair(700f, "¡Lanza manzanas podridas con el botón de DISPARAR!"),
                Pair(1300f, "¡Usa Salto Bomba (ABAJO en el aire) para romper cajas!")
            )
        )
    }

    /**
     * Nivel 2: Verano en el Maizal
     * Proyectiles de mazorca de maíz, fardos elásticos, laberintos de espigas.
     */
    private fun createLevel2Summer(): GameLevel {
        val platforms = mutableListOf<Platform>()
        val enemies = mutableListOf<Enemy>()
        val collectibles = mutableListOf<Collectible>()

        // Suelo principal
        platforms.add(Platform("g1", 0f, 680f, 850f, 120f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("g2", 950f, 680f, 1300f, 120f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("g3", 2350f, 680f, 1200f, 120f, PlatformType.SOLID_GROUND))

        // Fardos saltarines que impulsan hacia las copas de los molinos
        platforms.add(Platform("bhay1", 400f, 650f, 60f, 30f, PlatformType.BOUNCY_HAY))
        platforms.add(Platform("bhay2", 1400f, 650f, 60f, 30f, PlatformType.BOUNCY_HAY))
        platforms.add(Platform("bhay3", 2280f, 650f, 60f, 30f, PlatformType.BOUNCY_HAY))

        // Plataformas de molinos y espantapájaros
        platforms.add(Platform("p1", 420f, 430f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p2", 670f, 330f, 160f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p3", 1020f, 520f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p4", 1250f, 410f, 200f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p5", 1520f, 310f, 220f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p6", 1820f, 440f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p7", 2060f, 550f, 160f, 26f, PlatformType.WOOD_PLANK))

        // Cajas y vallas
        platforms.add(Platform("crate1", 1120f, 630f, 50f, 50f, PlatformType.BREAKABLE_CRATE))
        platforms.add(Platform("crate2", 1170f, 630f, 50f, 50f, PlatformType.BREAKABLE_CRATE))
        platforms.add(Platform("p8", 2500f, 520f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p9", 2760f, 400f, 200f, 26f, PlatformType.WOOD_PLANK))

        // Enemigos veloces
        enemies.add(
            Enemy(
                id = "e2_chibi1",
                type = EnemyType.CHIBI_FORK,
                x = 550f,
                y = 636f,
                patrolMinX = 460f,
                patrolMaxX = 820f,
                baseSpeed = 115f
            )
        )
        enemies.add(
            Enemy(
                id = "e2_rooster1",
                type = EnemyType.ROOSTER,
                x = 700f,
                y = 286f,
                patrolMinX = 670f,
                patrolMaxX = 820f,
                baseSpeed = 130f
            )
        )
        enemies.add(
            Enemy(
                id = "e2_chibi2",
                type = EnemyType.CHIBI_KNIFE,
                x = 1100f,
                y = 636f,
                patrolMinX = 980f,
                patrolMaxX = 1380f,
                baseSpeed = 130f
            )
        )
        enemies.add(
            Enemy(
                id = "e2_chibi3",
                type = EnemyType.CHIBI_FORK,
                x = 1600f,
                y = 266f,
                patrolMinX = 1530f,
                patrolMaxX = 1720f,
                baseSpeed = 100f
            )
        )
        enemies.add(
            Enemy(
                id = "e2_chibi4",
                type = EnemyType.CHIBI_KNIFE,
                x = 2550f,
                y = 636f,
                patrolMinX = 2400f,
                patrolMaxX = 2800f,
                baseSpeed = 140f
            )
        )

        // Coleccionables con MAZORCAS de maíz
        collectibles.add(Collectible("c1", CollectibleType.CORN, 240f, 620f))
        collectibles.add(Collectible("c2", CollectibleType.CORN, 480f, 380f))
        collectibles.add(Collectible("c3", CollectibleType.GOLD_COIN, 730f, 280f))
        collectibles.add(Collectible("c4", CollectibleType.CORN, 1080f, 470f))
        collectibles.add(Collectible("c5", CollectibleType.HEART, 1310f, 360f))
        collectibles.add(Collectible("c6", CollectibleType.ACORN, 1610f, 260f))
        collectibles.add(Collectible("c7", CollectibleType.CORN, 1880f, 390f))
        collectibles.add(Collectible("c8", CollectibleType.GOLD_COIN, 2120f, 500f))
        collectibles.add(Collectible("c9", CollectibleType.CORN, 2600f, 470f))
        collectibles.add(Collectible("c10", CollectibleType.ACORN, 2850f, 350f))

        return GameLevel(
            id = 2,
            season = Season.SUMMER,
            title = "Nivel 2: El Gran Maizal",
            subtitle = "¡El maíz explota como palomitas al impactar!",
            worldWidth = 3300f,
            worldHeight = 900f,
            playerStartX = 80f,
            playerStartY = 600f,
            exitX = 3150f,
            exitY = 600f,
            platforms = platforms,
            enemies = enemies,
            collectibles = collectibles,
            hints = listOf(
                Pair(150f, "¡Recoge mazorcas de maíz para disparos súper veloces!"),
                Pair(800f, "¡Los fardos amarillos de heno te dan un súper rebote!")
            )
        )
    }

    /**
     * Nivel 3: Otoño en la Huerta
     * Bolas de lodo, charcos resbalosos, hojas secas y viento.
     */
    private fun createLevel3Autumn(): GameLevel {
        val platforms = mutableListOf<Platform>()
        val enemies = mutableListOf<Enemy>()
        val collectibles = mutableListOf<Collectible>()

        // Suelo con parches resbaladizos de barro
        platforms.add(Platform("g1", 0f, 680f, 700f, 120f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("mud1", 700f, 680f, 300f, 120f, PlatformType.SLIPPERY_MUD))
        platforms.add(Platform("g2", 1000f, 680f, 600f, 120f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("mud2", 1600f, 680f, 350f, 120f, PlatformType.SLIPPERY_MUD))
        platforms.add(Platform("g3", 1950f, 680f, 1300f, 120f, PlatformType.SOLID_GROUND))

        // Plataformas en los árboles otoñales y molinos
        platforms.add(Platform("p1", 350f, 530f, 160f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p2", 580f, 410f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p3", 860f, 330f, 220f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p4", 1200f, 480f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p5", 1460f, 370f, 200f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p6", 1750f, 270f, 220f, 26f, PlatformType.WOOD_PLANK))

        // Cajas rompibles protegiendo bellotas doradas
        platforms.add(Platform("crate1", 1300f, 630f, 50f, 50f, PlatformType.BREAKABLE_CRATE))
        platforms.add(Platform("crate2", 1300f, 580f, 50f, 50f, PlatformType.BREAKABLE_CRATE))
        platforms.add(Platform("crate3", 1350f, 630f, 50f, 50f, PlatformType.BREAKABLE_CRATE))

        // Zona final
        platforms.add(Platform("p7", 2150f, 530f, 180f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p8", 2420f, 420f, 200f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("p9", 2700f, 520f, 180f, 26f, PlatformType.WOOD_PLANK))

        // Enemigos
        enemies.add(
            Enemy(
                id = "e3_chibi1",
                type = EnemyType.CHIBI_KNIFE,
                x = 500f,
                y = 636f,
                patrolMinX = 350f,
                patrolMaxX = 680f,
                baseSpeed = 120f
            )
        )
        enemies.add(
            Enemy(
                id = "e3_chibi2",
                type = EnemyType.CHIBI_FORK,
                x = 1100f,
                y = 636f,
                patrolMinX = 1010f,
                patrolMaxX = 1280f,
                baseSpeed = 125f
            )
        )
        enemies.add(
            Enemy(
                id = "e3_rooster1",
                type = EnemyType.ROOSTER,
                x = 1500f,
                y = 326f,
                patrolMinX = 1460f,
                patrolMaxX = 1640f,
                baseSpeed = 140f
            )
        )
        enemies.add(
            Enemy(
                id = "e3_chibi3",
                type = EnemyType.CHIBI_KNIFE,
                x = 2250f,
                y = 636f,
                patrolMinX = 2050f,
                patrolMaxX = 2600f,
                baseSpeed = 140f
            )
        )

        // Coleccionables: Bolas de lodo y bellotas
        collectibles.add(Collectible("c1", CollectibleType.MUD, 260f, 620f))
        collectibles.add(Collectible("c2", CollectibleType.MUD, 420f, 480f))
        collectibles.add(Collectible("c3", CollectibleType.ACORN, 640f, 360f))
        collectibles.add(Collectible("c4", CollectibleType.GOLD_COIN, 940f, 280f))
        collectibles.add(Collectible("c5", CollectibleType.ACORN, 1325f, 530f)) // dentro de cajas
        collectibles.add(Collectible("c6", CollectibleType.MUD, 1540f, 320f))
        collectibles.add(Collectible("c7", CollectibleType.HEART, 1830f, 220f))
        collectibles.add(Collectible("c8", CollectibleType.MUD, 2220f, 480f))
        collectibles.add(Collectible("c9", CollectibleType.GOLD_COIN, 2500f, 370f))
        collectibles.add(Collectible("c10", CollectibleType.ACORN, 2760f, 470f))

        return GameLevel(
            id = 3,
            season = Season.AUTUMN,
            title = "Nivel 3: Huerta Otoñal",
            subtitle = "¡Cuidado con el barro resbaladizo y las hojas al viento!",
            worldWidth = 3200f,
            worldHeight = 900f,
            playerStartX = 80f,
            playerStartY = 600f,
            exitX = 3050f,
            exitY = 600f,
            platforms = platforms,
            enemies = enemies,
            collectibles = collectibles,
            hints = listOf(
                Pair(150f, "¡Las bolas de lodo ralentizan y aturden a los enemigos!"),
                Pair(750f, "¡El barro marrón es resbaloso, calcula bien tus saltos!")
            )
        )
    }

    /**
     * Nivel 4: Invierno en el Gran Granero - ¡BATALLA CONTRA EL GRANJERO DON RAMIRO!
     * Arena cerrada de batalla con palancas de trampas rurales,
     * fardos de heno gigantes rodantes, horcas voladoras y fase de furia.
     */
    private fun createLevel4WinterBoss(): GameLevel {
        val platforms = mutableListOf<Platform>()
        val collectibles = mutableListOf<Collectible>()

        // Arena de combate (anchura compacta de arena 1600f)
        platforms.add(Platform("boss_floor", 0f, 680f, 1600f, 120f, PlatformType.SOLID_GROUND))

        // Muros laterales para contener la arena del granero
        platforms.add(Platform("wall_l", -40f, 0f, 60f, 800f, PlatformType.SOLID_GROUND))
        platforms.add(Platform("wall_r", 1580f, 0f, 60f, 800f, PlatformType.SOLID_GROUND))

        // Plataformas elevadas en la arena para esquivar y alcanzar palancas
        platforms.add(Platform("bp1", 160f, 520f, 200f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("bp2", 420f, 400f, 220f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("bp3", 700f, 300f, 200f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("bp4", 980f, 400f, 220f, 26f, PlatformType.WOOD_PLANK))
        platforms.add(Platform("bp5", 1240f, 520f, 200f, 26f, PlatformType.WOOD_PLANK))

        // Fardos de heno elásticos en las esquinas
        platforms.add(Platform("bhay_l", 60f, 650f, 60f, 30f, PlatformType.BOUNCY_HAY))
        platforms.add(Platform("bhay_r", 1480f, 650f, 60f, 30f, PlatformType.BOUNCY_HAY))

        // Palanca de trampa de heno en la plataforma central superior
        platforms.add(Platform("trap_lever", 770f, 265f, 60f, 35f, PlatformType.LEVER_TRAP))

        // Cajas de suministros
        platforms.add(Platform("crate_l", 220f, 470f, 50f, 50f, PlatformType.BREAKABLE_CRATE))
        platforms.add(Platform("crate_r", 1330f, 470f, 50f, 50f, PlatformType.BREAKABLE_CRATE))

        // Munición constante para arrojar al granjero
        collectibles.add(Collectible("c1", CollectibleType.APPLE, 220f, 420f))
        collectibles.add(Collectible("c2", CollectibleType.CORN, 510f, 350f))
        collectibles.add(Collectible("c3", CollectibleType.MUD, 1070f, 350f))
        collectibles.add(Collectible("c4", CollectibleType.ACORN, 780f, 220f))
        collectibles.add(Collectible("c5", CollectibleType.APPLE, 1330f, 420f))
        collectibles.add(Collectible("c6", CollectibleType.HEART, 790f, 620f))

        val boss = Boss(
            name = "Don Ramiro",
            x = 1100f,
            y = 588f,
            width = 80f,
            height = 96f,
            health = 12,
            maxHealth = 12,
            facingRight = false
        )

        return GameLevel(
            id = 4,
            season = Season.WINTER,
            title = "Nivel 4: ¡La Furia de Don Ramiro!",
            subtitle = "Supera sus trampas rurales y derrota al granjero para ser libre.",
            worldWidth = 1600f,
            worldHeight = 900f,
            playerStartX = 120f,
            playerStartY = 600f,
            exitX = 1500f,
            exitY = 600f,
            platforms = platforms,
            enemies = emptyList(),
            collectibles = collectibles,
            boss = boss,
            hints = listOf(
                Pair(140f, "¡Don Ramiro arroja horcas y hace rodar fardos de heno gigantes!"),
                Pair(600f, "¡Salta sobre la palanca superior para soltarle una trampa de heno!")
            )
        )
    }
}
