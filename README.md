### Name: Nguyen Nhat An
### Id: 22110007
# Image Loader App

A simple Android app that allows users to load images from URLs and display them in the app. The app supports two methods for loading images: `AsyncTask` and `AsyncTaskLoader`. It also includes functionality to detect internet connectivity and shows relevant status messages, such as "No Internet Connection" or "Image Loaded Successfully".

The app uses modern UI components based on **Jetpack Compose** and adheres to **Material Design** principles.

## Features

- **Image Loading**: Load images from URLs and display them in the app.
- **AsyncTask & AsyncTaskLoader**: Two methods for loading images in the background.
- **Internet Connectivity Check**: Detect if the device is connected to the internet and update UI accordingly.
- **Loading States**: Display appropriate loading, success, and error states.
- **Background Service**: A foreground service that runs in the background to show notifications (you can expand this part as needed).

## Prerequisites

To run this app, you need to have the following:

- **Android Studio** installed on your machine.
- A physical or virtual Android device/emulator running at least **Android 5.0 (Lollipop)**.

## Installation

### 1. Clone the repository

Clone this repository to your local machine using the following command:

```bash
git clone https://github.com/yourusername/imageloader.git
```

### 2. Open the project
Open Android Studio.

Click on Open an existing project and select the folder where you cloned the repository.

### 3. Install dependencies
Android Studio will automatically prompt you to install necessary dependencies. Click Sync Now in the toolbar to sync the project.

### 4. Run the app
Select an emulator or a connected device.

Click on the Run button (green triangle) in Android Studio to build and run the app on your device.

## How to Use
### 1. Enter an Image URL: In the "Image URL" field, type a valid URL that points to an image (e.g., https://picsum.photos/200).

### 2. Choose Loading Method: You can choose to load the image using AsyncTask or AsyncTaskLoader:

+ AsyncTask: The default background task used for loading the image.

+ AsyncTaskLoader: A more robust method that handles configuration changes and lifecycle better.

### 3. Load Image: Click the Load Image button to start loading the image.

+ The app will display a loading indicator while fetching the image.

+ Once the image is loaded, it will be displayed in the app.

+ If the image fails to load, an error message will appear.

### 4. Internet Connectivity: 
The app automatically detects whether the device is connected to the internet. If not, it will disable the Load Image button and display a "No Internet Connection" message.

