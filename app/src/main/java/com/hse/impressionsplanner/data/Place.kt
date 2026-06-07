package com.hse.impressionsplanner.data

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val address: String
)

val samplePlaces = listOf(
    Place(
        "1",
        "Третьяковская галерея",
        "Крупнейший музей русского искусства",
        "Музеи",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/GTG_Moscow_August_2009.jpg/800px-GTG_Moscow_August_2009.jpg",
        "Лаврушинский пер., 10"
    ),
    Place(
        "2",
        "Парк Горького",
        "Главный парк культуры и отдыха Москвы",
        "Парки",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Park_Gor%27kogo.jpg/800px-Park_Gor%27kogo.jpg",
        "Крымский Вал, 9"
    ),
    Place(
        "3",
        "Кафе Пушкинъ",
        "Легендарный московский ресторан русской кухни",
        "Рестораны",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Cafe_Pushkin_Moscow.jpg/800px-Cafe_Pushkin_Moscow.jpg",
        "Тверской бул., 26А"
    ),
    Place(
        "4",
        "Красная площадь",
        "Главная площадь России, объект ЮНЕСКО",
        "Исторические места",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Red_Square%2C_Moscow%2C_Russia.jpg/800px-Red_Square%2C_Moscow%2C_Russia.jpg",
        "Красная пл., 1"
    ),
    Place(
        "5",
        "Царицыно",
        "Дворцово-парковый ансамбль XVIII века",
        "Парки",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8b/Tsaritsyno_palace_big_palace.jpg/800px-Tsaritsyno_palace_big_palace.jpg",
        "ул. Дольская, 1"
    ),
    Place(
        "6",
        "Музей космонавтики",
        "История освоения космоса и техника",
        "Музеи",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Memorial_Museum_of_Cosmonautics_%28Moscow%29.jpg/800px-Memorial_Museum_of_Cosmonautics_%28Moscow%29.jpg",
        "просп. Мира, 111"
    ),
    Place(
        "7",
        "Воробьёвы горы",
        "Смотровая площадка с панорамой города",
        "Исторические места",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Moscow_from_Sparrow_Hills.jpg/800px-Moscow_from_Sparrow_Hills.jpg",
        "Воробьёвское ш., 2"
    ),
    Place(
        "8",
        "Лужники",
        "Главный спортивный комплекс страны",
        "Развлечения",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Luzhniki_Stadium_2018.jpg/800px-Luzhniki_Stadium_2018.jpg",
        "Лужники, 24"
    ),
    Place(
        "9",
        "Пушкинский музей",
        "Крупнейший музей западного искусства",
        "Музеи",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/GMII_im_Pushkina.jpg/800px-GMII_im_Pushkina.jpg",
        "ул. Волхонка, 12"
    ),
    Place(
        "10",
        "Измайловский парк",
        "Крупнейший лесопарк Москвы",
        "Парки",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Izmaylovsky_Park.jpg/800px-Izmaylovsky_Park.jpg",
        "Народный просп., 17"
    )
)

val categories = listOf("Все", "Музеи", "Парки", "Рестораны", "Исторические места", "Развлечения")

data class Route(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val duration: String,
    val placeCount: Int
)

val sampleRoutes = listOf(
    Route(
        "r1",
        "Классическая Москва",
        "Красная площадь, Кремль, ГУМ — обязательная программа для каждого",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Red_Square%2C_Moscow%2C_Russia.jpg/800px-Red_Square%2C_Moscow%2C_Russia.jpg",
        "4 часа",
        5
    ),
    Route(
        "r2", "Музейный weekend", "Третьяковка, Пушкинский музей, Музей космонавтики",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/GTG_Moscow_August_2009.jpg/800px-GTG_Moscow_August_2009.jpg",
        "6 часов", 3
    ),
    Route(
        "r3", "Парки и воздух", "Парк Горького, Воробьёвы горы, Царицыно — прогулка на природе",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Park_Gor%27kogo.jpg/800px-Park_Gor%27kogo.jpg",
        "5 часов", 3
    ),
    Route(
        "r4", "Историческая прогулка", "Измайлово, Царицыно, Воробьёвы горы",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Moscow_from_Sparrow_Hills.jpg/800px-Moscow_from_Sparrow_Hills.jpg",
        "7 часов", 3
    ),
    Route(
        "r5", "Гастрономический тур", "Лучшие рестораны и кафе в центре Москвы",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Cafe_Pushkin_Moscow.jpg/800px-Cafe_Pushkin_Moscow.jpg",
        "3 часа", 4
    )
)
