package com.hse.impressionsplanner.data

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val address: String,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

val samplePlaces = listOf(
    Place(
        "1", "Третьяковская галерея",
        "Крупнейший музей русского искусства",
        "Музеи",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/GTG_Moscow_August_2009.jpg/800px-GTG_Moscow_August_2009.jpg",
        "Лаврушинский пер., 10",
        55.7414, 37.6208
    ),
    Place(
        "2", "Парк Горького",
        "Главный парк культуры и отдыха Москвы",
        "Парки",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Park_Gor%27kogo.jpg/800px-Park_Gor%27kogo.jpg",
        "Крымский Вал, 9",
        55.7298, 37.6022
    ),
    Place(
        "3", "Кафе Пушкинъ",
        "Легендарный московский ресторан русской кухни",
        "Рестораны",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Cafe_Pushkin_Moscow.jpg/800px-Cafe_Pushkin_Moscow.jpg",
        "Тверской бул., 26А",
        55.7635, 37.6057
    ),
    Place(
        "4", "Красная площадь",
        "Главная площадь России, объект ЮНЕСКО",
        "Исторические места",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Red_Square%2C_Moscow%2C_Russia.jpg/800px-Red_Square%2C_Moscow%2C_Russia.jpg",
        "Красная пл., 1",
        55.7539, 37.6208
    ),
    Place(
        "5", "Царицыно",
        "Дворцово-парковый ансамбль XVIII века",
        "Парки",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8b/Tsaritsyno_palace_big_palace.jpg/800px-Tsaritsyno_palace_big_palace.jpg",
        "ул. Дольская, 1",
        55.6213, 37.6793
    ),
    Place(
        "6", "Музей космонавтики",
        "История освоения космоса и техника",
        "Музеи",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Memorial_Museum_of_Cosmonautics_%28Moscow%29.jpg/800px-Memorial_Museum_of_Cosmonautics_%28Moscow%29.jpg",
        "просп. Мира, 111",
        55.8196, 37.6394
    ),
    Place(
        "7", "Воробьёвы горы",
        "Смотровая площадка с панорамой города",
        "Исторические места",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Moscow_from_Sparrow_Hills.jpg/800px-Moscow_from_Sparrow_Hills.jpg",
        "Воробьёвское ш., 2",
        55.7092, 37.5451
    ),
    Place(
        "8", "Лужники",
        "Главный спортивный комплекс страны",
        "Развлечения",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Luzhniki_Stadium_2018.jpg/800px-Luzhniki_Stadium_2018.jpg",
        "Лужники, 24",
        55.7154, 37.5541
    ),
    Place(
        "9", "Пушкинский музей",
        "Крупнейший музей западного искусства",
        "Музеи",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/GMII_im_Pushkina.jpg/800px-GMII_im_Pushkina.jpg",
        "ул. Волхонка, 12",
        55.7476, 37.6067
    ),
    Place(
        "10", "Измайловский парк",
        "Крупнейший лесопарк Москвы",
        "Парки",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Izmaylovsky_Park.jpg/800px-Izmaylovsky_Park.jpg",
        "Народный просп., 17",
        55.7903, 37.7903
    )
)

val categories = listOf("Все", "Музеи", "Парки", "Рестораны", "Исторические места", "Развлечения")

data class Route(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val duration: String,
    val placeCount: Int,
    val sourceUrl: String = "",
    val categories: List<String> = emptyList(),
    val points: List<String> = emptyList(),
    val authorContact: String = ""
)

val sampleRoutes = listOf(
    Route(
        id = "r1",
        name = "Классическая Москва",
        description = "Красная площадь, Кремль, ГУМ — обязательная программа для каждого",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Red_Square%2C_Moscow%2C_Russia.jpg/800px-Red_Square%2C_Moscow%2C_Russia.jpg",
        duration = "4 часа", placeCount = 5,
        categories = listOf("Культура", "Исторический"),
        points = listOf("Красная площадь", "Кремль и Александровский сад", "ГУМ", "Манежная площадь", "Никольская улица"),
        authorContact = "moscowwalks.ru"
    ),
    Route(
        id = "r2",
        name = "Музейный weekend",
        description = "Третьяковка, Пушкинский музей, Музей космонавтики",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/GTG_Moscow_August_2009.jpg/800px-GTG_Moscow_August_2009.jpg",
        duration = "6 часов", placeCount = 3,
        categories = listOf("Культура", "Спокойный"),
        points = listOf("Третьяковская галерея", "Пушкинский музей изобразительных искусств", "Музей космонавтики"),
        authorContact = "culture.moscow"
    ),
    Route(
        id = "r3",
        name = "Парки и воздух",
        description = "Парк Горького, Воробьёвы горы, Царицыно — прогулка на природе",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Park_Gor%27kogo.jpg/800px-Park_Gor%27kogo.jpg",
        duration = "5 часов", placeCount = 3,
        categories = listOf("Природа", "Спокойный"),
        points = listOf("Парк Горького", "Воробьёвы горы (смотровая)", "Царицыно"),
        authorContact = "parki.moscow"
    ),
    Route(
        id = "r4",
        name = "Историческая прогулка",
        description = "Измайлово, Царицыно, Воробьёвы горы",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Moscow_from_Sparrow_Hills.jpg/800px-Moscow_from_Sparrow_Hills.jpg",
        duration = "7 часов", placeCount = 3,
        categories = listOf("Исторический", "Спокойный", "Природа"),
        points = listOf("Измайловский Кремль", "Царицыно", "Воробьёвы горы"),
        authorContact = "history-walks.ru"
    ),
    Route(
        id = "r5",
        name = "Гастрономический тур",
        description = "Лучшие рестораны и кафе в центре Москвы",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Cafe_Pushkin_Moscow.jpg/800px-Cafe_Pushkin_Moscow.jpg",
        duration = "3 часа", placeCount = 4,
        categories = listOf("Гастрономия"),
        points = listOf("Кафе Пушкинъ", "Ресторан Brasserie Мост", "Депо. Москва", "Рынок Усачевский"),
        authorContact = "restoranoff.ru"
    )
)
