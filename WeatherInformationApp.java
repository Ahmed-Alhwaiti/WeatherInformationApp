import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherInformationApp extends JFrame {


    private static final String API_KEY = "22f44d2ff644186f135577fe52a7b266";

    private final JTextField cityField;
    private final JComboBox<String> unitBox;
    private final JTextArea currentWeatherArea;
    private final JTextArea forecastArea;
    private final DefaultListModel<String> historyModel;
    private final HttpClient httpClient;

    public WeatherInformationApp() {
        setTitle("Weather Information App");
        setSize(760, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        httpClient = HttpClient.newHttpClient();

        cityField = new JTextField(20);
        unitBox = new JComboBox<>(new String[]{"Metric: Celsius / m/s", "Imperial: Fahrenheit / mph"});
        JButton searchButton = new JButton("Search Weather");

        Font appFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font emojiFont = getBestEmojiFont(16);

        currentWeatherArea = new JTextArea(8, 35);
        currentWeatherArea.setEditable(false);
        currentWeatherArea.setFont(emojiFont);
        currentWeatherArea.setLineWrap(true);
        currentWeatherArea.setWrapStyleWord(true);

        forecastArea = new JTextArea(10, 35);
        forecastArea.setEditable(false);
        forecastArea.setFont(emojiFont);
        forecastArea.setLineWrap(true);
        forecastArea.setWrapStyleWord(true);

        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(appFont);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter City:"));
        topPanel.add(cityField);
        topPanel.add(unitBox);
        topPanel.add(searchButton);

        JPanel weatherPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        weatherPanel.add(new JScrollPane(currentWeatherArea));
        weatherPanel.add(new JScrollPane(forecastArea));

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Recent Search History"));
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        setLayout(new BorderLayout(10, 10));
        add(topPanel, BorderLayout.NORTH);
        add(weatherPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.SOUTH);

        setDynamicBackground();

        searchButton.addActionListener((ActionEvent e) -> searchWeather());
    }

    private void searchWeather() {
        String city = cityField.getText().trim();

        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a city name.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String unitValue = unitBox.getSelectedIndex() == 0 ? "metric" : "imperial";

        try {
            displayCurrentWeather(city, unitValue);
            displayForecast(city, unitValue);
            addToHistory(city);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not retrieve weather data. Please check the city name, API key, or internet connection.\n\nDetails: "
                            + ex.getMessage(),
                    "Weather Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayCurrentWeather(String city, String units) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + encodedCity + "&appid=" + API_KEY + "&units=" + units;

        String response = sendRequest(url);
        JSONObject json = new JSONObject(response);

        if (json.has("cod") && !json.get("cod").toString().equals("200")) {
            String message = json.optString("message", "Invalid API response");
            throw new IOException(message);
        }

        JSONObject main = json.getJSONObject("main");
        JSONObject wind = json.getJSONObject("wind");
        JSONArray weatherArray = json.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);

        String cityName = json.getString("name");
        String condition = weather.getString("description");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        double windSpeed = wind.getDouble("speed");

        String temperatureSymbol = units.equals("metric") ? "°C" : "°F";
        String windUnit = units.equals("metric") ? "m/s" : "mph";
        String icon = getWeatherIcon(condition);

        currentWeatherArea.setText(
                "Current Weather\n" +
                        "-----------------------------\n" +
                        "City: " + cityName + "\n" +
                        "Condition: " + icon + " " + condition + "\n" +
                        "Temperature: " + temperature + temperatureSymbol + "\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Wind Speed: " + windSpeed + " " + windUnit + "\n"
        );

        currentWeatherArea.setCaretPosition(0);
    }

    private void displayForecast(String city, String units) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "https://api.openweathermap.org/data/2.5/forecast?q="
                + encodedCity + "&appid=" + API_KEY + "&units=" + units + "&cnt=5";

        String response = sendRequest(url);
        JSONObject json = new JSONObject(response);

        if (json.has("cod") && !json.get("cod").toString().equals("200")) {
            String message = json.optString("message", "Invalid forecast response");
            throw new IOException(message);
        }

        JSONArray forecastList = json.getJSONArray("list");
        String temperatureSymbol = units.equals("metric") ? "°C" : "°F";

        StringBuilder forecastText = new StringBuilder();
        forecastText.append("Short-Term Forecast\n");
        forecastText.append("-----------------------------\n");

        for (int i = 0; i < forecastList.length(); i++) {
            JSONObject item = forecastList.getJSONObject(i);
            JSONObject main = item.getJSONObject("main");
            JSONArray weatherArray = item.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);

            String dateTime = item.getString("dt_txt");
            double temp = main.getDouble("temp");
            String condition = weather.getString("description");
            String icon = getWeatherIcon(condition);

            forecastText.append(dateTime)
                    .append(" | ")
                    .append(icon)
                    .append(" ")
                    .append(condition)
                    .append(" | ")
                    .append(temp)
                    .append(temperatureSymbol)
                    .append("\n");
        }

        forecastArea.setText(forecastText.toString());
        forecastArea.setCaretPosition(0);
    }

    private String sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API request failed with status code: " + response.statusCode());
        }

        return response.body();
    }

    private String getWeatherIcon(String condition) {
        condition = condition.toLowerCase();

        if (condition.contains("clear")) {
            return "☀";
        } else if (condition.contains("cloud")) {
            return "☁";
        } else if (condition.contains("rain") || condition.contains("drizzle")) {
            return "🌧";
        } else if (condition.contains("snow")) {
            return "❄";
        } else if (condition.contains("storm") || condition.contains("thunder")) {
            return "⛈";
        } else if (condition.contains("mist") || condition.contains("fog")) {
            return "☁";
        } else {
            return "☀";
        }
    }

    private Font getBestEmojiFont(int size) {
        String[] possibleFonts = {
                "Segoe UI Emoji",
                "Segoe UI Symbol",
                "Noto Color Emoji",
                "Apple Color Emoji",
                "Dialog"
        };

        for (String fontName : possibleFonts) {
            Font font = new Font(fontName, Font.PLAIN, size);

            if (font.canDisplayUpTo("☀ ☁ 🌧 ❄ ⛈") == -1) {
                return font;
            }
        }

        return new Font("Segoe UI Symbol", Font.PLAIN, size);
    }

    private void addToHistory(String city) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        historyModel.addElement(city + " searched at " + time);
    }

    private void setDynamicBackground() {
        LocalTime now = LocalTime.now();
        Color backgroundColor;

        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(12, 0))) {
            backgroundColor = new Color(22, 140, 229);
        } else if (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(18, 0))) {
            backgroundColor = new Color(244, 196, 25);
        } else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(21, 0))) {
            backgroundColor = new Color(255, 220, 180);
        } else {
            backgroundColor = new Color(210, 215, 230);
        }

        getContentPane().setBackground(backgroundColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherInformationApp app = new WeatherInformationApp();
            app.setVisible(true);
        });
    }
}