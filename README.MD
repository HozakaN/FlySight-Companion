# FlySight Companion

FlySight Companion is an Android application to manage FlySight configuration files through Bluetooth.

## Features

- **Create and manage FlySight configuration files on your phone**: Easily create, edit, and manage configuration files for your FlySight device directly from your Android phone.
- **Connect to FlySight through Bluetooth**: Connect to your FlySight device via Bluetooth to:
    - Check and change the current configuration file
    - Count the number of record files
    - Navigate the file tree

## Getting Started

### Prerequisites

- Android device with Bluetooth Low Energy support
- A FlySight 2 device

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/HozakaN/flysight-companion.git
    ```
2. Open the project in Android Studio.
3. Build and run the project on your Android device.

## Usage

Device management :
1. Open the FlySight Companion app on your Android device.
2. Pair your FlySight device with your Android device.
3. The app will scan for FlySight 2 devices. If one is already paired to the phone, it will appear in the application
4. Connect to the device through the dedicated screen

Configuration management :
1. From the home screen, navigate to the right tab to find a home for your configuration files.
2. You can create edit and delete configuration files.
3. Those files can be pushed to your FlySight when connected.

## License

This project is licensed under the Creative Commons BY-NC 4.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Mike for his work on FlySight 1 and 2 and his help on understanding how they work under the hood.