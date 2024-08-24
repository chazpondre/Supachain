Getting Started with LocalAI
---
### 1. Install Docker

First, ensure Docker is installed on your machine:

- [Download Docker](https://www.docker.com/get-started) for your operating system.
- Follow the installation guide for your OS:
    - **[Windows](https://docs.docker.com/desktop/install/windows-install/)**
    - **[macOS](https://docs.docker.com/desktop/install/mac-install/)**
    - **[Linux](https://docs.docker.com/engine/install/)**

Once installed, verify Docker is running by entering the following command in your terminal:

```bash
docker --version
```

If Docker is installed, this command will display the version number.

---

### 2.0 Set Up Ollama

Ollama provides an easy way to run large language models on your system. Follow the steps below based on your operating system:

#### 2.0.1 Installation Instructions

- **macOS**: [Download Ollama](https://ollama.com/download/Ollama-darwin.zip)
- **Windows** (Preview): [Download Ollama](https://ollama.com/download/OllamaSetup.exe)
- **Linux**: Run the following command in your terminal:

```bash
curl -fsSL https://ollama.com/install.sh | sh
```

For manual installation instructions, visit the [Ollama GitHub page](https://github.com/ollama/ollama/blob/main/docs/linux.md).

You can also use the [official Docker image](https://hub.docker.com/r/ollama/ollama) by running:

```bash
docker run --name ollama -ti ollama/ollama
```

#### 2.0.2 Running Ollama on Windows GPU

To run Ollama on Windows with a NVIDIA GPU, you may need to set up the app using the NVIDIA Control Panel. Here’s a step-by-step guide:

1. **Download and Install Ollama:**
    - Use the [Ollama installer](https://ollama.com/download) for Windows.

   > **Tip: Set Up GPU Usage via NVIDIA Control Panel**
   > - Open the **NVIDIA Control Panel** (right-click on your desktop and select it from the context menu).
   > - Go to **Manage 3D Settings** > **Program Settings**.
   > - Add the **Ollama** app from your installation directory.
   > - Under **Select the preferred graphics processor**, choose **High-performance NVIDIA processor**.
   > - Apply the changes.

2. **Run Ollama:**
    - Once configured, you can run Ollama commands in your terminal, and it will leverage your NVIDIA GPU automatically.

Make sure your GPU drivers are up-to-date for optimal performance.

### 2.1. Set Up LocalAI

#### 2.1.1 Check Your CUDA Version (Optional, for GPU Support)

If you plan to use LocalAI with NVIDIA GPUs, you'll need to check which CUDA version is installed on your system.

- Run the following command to check your CUDA version:

```bash
nvidia-smi
```

Look for the "CUDA Version" at the top of the output. It will show something like:

```
CUDA Version: 11.4
```


---

#### 2.1.2 Set up LocalAI (☕ May take a while)

Next, run LocalAI using Docker. The command depends on whether you have an NVIDIA, Intel, or AMD GPU, and which CUDA version you have.

**For systems without GPUs (CPU only):**

```bash
docker run -p 8080:8080 --name local-ai -ti localai/localai:latest-cpu
```

[//]: # (Convert to tabs in the future)
**For systems with NVIDIA GPUs:**

1. **If your CUDA version is 11:**

```bash
docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-gpu-nvidia-cuda-11
```

2. **If your CUDA version is 12:**

```bash
docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-gpu-nvidia-cuda-12
```

**For systems with Intel GPUs:**

1. **If you need SYCL support (f16):**

```bash
docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-gpu-intel-f16
```

2. **If you need SYCL support (f32):**

```bash
docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-gpu-intel-f32
```

**For systems with AMD GPUs:**

```bash
docker run -p 8080:8080 --gpus all --name local-ai -ti localai/localai:latest-gpu-hipblas
```

These commands will start the LocalAI container and map port 8080 on your local machine to the container.

---

#### 2.1.3 Download a Model

After starting the LocalAI container, you can download a model via the web interface.

1. **Open Your Web Browser** and go to:

   ```
   http://localhost:8080/browse/
   ```

2. **Browse and Select the Model**:
  - Navigate through the available models (e.g. find `meta-llama-3.1-8b-instruct`).
  - Click on the model name or corresponding download button to start the download.

3. **Wait for the Download to Complete**:
  - The download progress will be displayed on the web page.
  - Ensure the download is fully completed before closing the browser or stopping the Docker container.

This will download and make the selected model available for use with LocalAI.

---



