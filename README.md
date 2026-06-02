# Weather Information App

## Description

The Weather Information App is a Java desktop application that allows users to search for real-time weather information by entering a city name. The application uses Java Swing for the graphical user interface and OpenWeatherMap API for retrieving weather data.

## Main Features

- Search weather by city name
- Display temperature, humidity, wind speed, and weather condition
- Show a short-term weather forecast
- Switch between metric and imperial units
- Display simple weather icons
- Track recent searches with timestamps
- Change background color depending on the time of day
- Handle invalid input and failed API requests

## Technologies Used

- Java
- Java Swing
- Java HttpClient
- OpenWeatherMap API
- JSON parsing using org.json library

## How to Run the App

1. Install Java JDK 11 or later.
2. Download the `json.jar` library for JSON parsing.
3. Create a free account at OpenWeatherMap.
4. Generate an API key from your OpenWeatherMap account.
5. Open `WeatherInformationApp.java`.
6. Replace:

```java
YOUR_API_KEY_HERE
