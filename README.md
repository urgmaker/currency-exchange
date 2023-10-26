## Проект "Обмен валют"

REST API для описания валют и обменных курсов. Позволяет просматривать и редактировать списки валют и обменных курсов, и совершать расчёт конвертации произвольных сумм из одной валюты в другую.

Веб-интерфейс для проекта не подразумевается.

Автор тех. задания: https://github.com/zhukovsd


## Что нужно знать

- Java - коллекции, ООП
- Maven/Gradle
- `javax` сервлеты
- HTTP - GET и POST запросы, коды ответа
- SQL, JDBC
- REST API, JSON

Фреймворки не используем.

## Мотивация проекта

- REST API - правильное именование ресурсов, использование HTTP кодов ответа
- SQL - базовый синтаксис, создание таблиц

## Структура базы данных

### `Currencies`

| Колонка | Тип | Комментарий |
| --- | --- | --- |
| ID | int | Айди валюты, автоинкремент, первичный ключ |
| Code | Varchar | Код валюты |
| FullName | Varchar | Полное имя валюты |
| Sign | Varchar | Символ валюты |

Пример записи в таблице для австралийского доллара:
| ID | Code | FullName | Sign |
| --- | --- | --- | --- |
| 1 | AUD | Australian dollar | A$ |


Индексы:
- Первичный ключ по полю ID
- Unique индекс по полю Code для гарантий уникальности валюты в таблице, и для ускорения поиска валюты по её аббривеатуре

### `ExchangeRates`

| Колонка | Тип | Комментарий |
| --- | --- | --- |
| ID | int | Айди курса обмена, автоинкремент, первичный ключ |
| BaseCurrencyId | int | ID базовой валюты|
| TargetCurrencyId | int | ID целевой валюты |
| Rate | Decimal(6) | Курс обмена единицы базовой валюты к единице целевой валюты |

Индексы:
- Первичный ключ по полю ID
- Unique индекс по паре полей BaseCurrencyId, TargetCurrencyId для гарантий уникальности валютной пары, и для ускорения поиска курса по паре валют

## REST API

Методы REST API реализуют [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) интерфейс над базой данных - позволяют создавать (C - craete), читать (R - read), редактировать (U - update). В целях упрощения, опустим удаление (D - delete).

### Валюты

#### GET `/currencies`

Получение списка валют. Пример ответа:
```
[
    "currency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },   
    "currency": {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
]
```

HTTP коды ответов:
- Успех - 200
- Ошибка (например, база данных недоступна) - 500

#### GET `/currency/USD`

Получение конкретной валюты. Пример ответа:
```
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```

HTTP коды ответов:
- Успех - 200
- Код валюты отстствует в адресе - 400
- Валюта не найдена - 404
- Ошибка (например, база данных недоступна) - 500

#### POST `/currencies`

Добавление новой валюты в базу. Данные передаются в теле запроса в виде полей формы (`multipart/form-data`). Поля формы - `name`, `code`, `sign`. Пример ответа - JSON представление вставленной в базу записи, включая её ID:
```
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```

HTTP коды ответов:
- Успех - 200
- Отсутствует нужное поле формы - 400
- Валюта с таким кодом уже существует - 409
- Ошибка (например, база данных недоступна) - 500

### Обменные курсы

#### GET `/exchangeRates`

Получение списка всех обменных курсов. Пример ответа:
```
[
    "id": 0,
    "exchangeRate": {
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```

HTTP коды ответов:
- Успех - 200
- Ошибка (например, база данных недоступна) - 500

#### GET `/exchangeRate/USDRUB`

Получение конкретного обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. Пример ответа:
```
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}

```

HTTP коды ответов:
- Успех - 200
- Коды валют пары отсутствуют в адресе - 400
- Обменный курс для пары не найден - 404
- Ошибка (например, база данных недоступна) - 500

#### POST `/exchangeRates`

Добавление нового обменного курса в базу. Данные передаются в теле запроса в виде полей формы (`multipart/form-data`). Поля формы - `baseCurrencyCode`, `targetCurrencyCode`, `rate`. Пример полей формы:
- `baseCurrencyCode` - USD
- `targetCurrencyCode` - EUR
- `rate` - 0.99

Пример ответа - JSON представление вставленной в базу записи, включая её ID:
```
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```

HTTP коды ответов:
- Успех - 200
- Отсутствует нужное поле формы - 400
- Валютная пара с таким кодом уже существует - 409
- Ошибка (например, база данных недоступна) - 500

#### PATCH `/exchangeRate/USDRUB`

Обновление существующего в базе обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. Данные передаются в теле запроса в виде полей формы (`multipart/form-data`). Единственное поле формы - `rate`.

Пример ответа - JSON представление обновлённой записи в базе данных, включая её ID:
```
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}

```

HTTP коды ответов:
- Успех - 200
- Отсутствует нужное поле формы - 400
- Валютная пара отсутствует в базе данных - 404
- Ошибка (например, база данных недоступна) - 500

### Обмен валюты

#### GET `/exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`

Рассчёт перевода определённого количества средств из одной валюты в другую. Пример запроса - GET `/exchange?from=USD&to=AUD&amount=10`.

Пример ответа:
```
{
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 1.45,
    "amount": 10.00
    "convertedAmount": 14.50
}
```

Получение курса для обмена может пройти по одному из двух сценариев. Допустим, совершаем перевод из валюты **A** в валюту **B**:
1. В таблице `ExchangeRates` существует валютная пара **AB** - берём её курс
2. В таблице `ExchangeRates` существует валютная пара **BA** - берем её курс, и считаем обратный, чтобы получить **AB**

Остальные возможные сценарии, для упрощения, опустим.

---

Для всех запросов, в случае ошибки, ответ может выглядеть так:
```
{
    "message": "Валюта не найдена"
}
```

Значение `message` зависит от того, какая именно ошибка произошла.
