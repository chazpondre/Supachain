# IMPORTANT:
🚨 **Important Notice**

It has come to my attention that fraudulent accounts — including some impersonating me on platforms like x.ai/twitter — are falsely claiming to represent me and are promoting cryptocurrency scams.

**I want to make it explicitly clear:**

* I do **not** have an account on x.ai, twitter or any affiliation with such platforms.
* I am **not** involved in the promotion, launch, or creation of any cryptocurrency.
* This repository is in no way connected to any crypto project.

Please report any suspicious activity or impersonation attempts. Thank you for your vigilance.
--


# Supachain

Welcome to **Supachain**, a comprehensive solution for building AI-powered applications. Our library provides a seamless way to integrate AI models, whether running locally or via remote providers, into your projects. As we approach our first official release, version 0.1.0, this README will guide you through the setup process and showcase how to utilize Supachain effectively.

## Table of Contents

1. [Getting Started](#getting-started)
   - [Set Up Local Providers](#1-set-up-local-providers)
   - [Ollama Setup](#110-set-up-ollama)
   - [LocalAI Setup](#120-set-up-localai)
2. [Examples](#2-examples)
   - [Simple Example using Ollama](#21-a-simple-example-using-ollama)
   - [Customizing Your Local Provider](#22-customizing-your-local-provider)
   - [Days of the Week Example with LocalAI](#23-days-of-the-week-example-with-localai)
   - [Simple Math Operations in Supachain](#24-using-simple-math-operations-in-supachain)
   - [Using Tools in Supachain with LocalAI](#25-using-tools-in-supachain-with-localai)

## Getting Started

### 1. Set Up Local Providers

You can skip this step if you're using remote models.

#### Providers

In the context of AI, providers are companies or organizations that offer AI models and services. Some popular providers include OpenAI, Gemini, and Gemma, while for local models, you can use LocalAI and Ollama. We also plan to release our native provider in a future release.

#### Local Models

Running AI models locally offers enhanced privacy, performance, and cost-effectiveness. You can choose to use Ollama or LocalAI as your providers. Here are some benefits:

- **Privacy:** Keep data local.
- **Performance:** Reduce latency.
- **Cost:** Avoid cloud fees.

### 1.1.0 Set Up Ollama

Ollama provides an easy way to run large language models on your system. Follow the steps below based on your operating system:

#### 1.1.1 Installation Instructions

- **macOS**: [Download Ollama](https://ollama.com/download/Ollama-darwin.zip)
- **Windows** (Preview): [Download Ollama](https://ollama.com/download/OllamaSetup.exe)
- **Linux**: Run the following command in your terminal:
  
  ```bash
  curl -fsSL https://ollama.com/install.sh | sh
  ```

For more detailed installation instructions, visit the [Ollama GitHub page](https://github.com/ollama/ollama/blob/main/docs/linux.md). Alternatively, you can use the [official Docker image](https://hub.docker.com/r/ollama/ollama) by running:

```bash
docker run --name ollama -ti ollama/ollama
```

#### 1.1.2 Running Ollama on Windows GPU

To run Ollama on Windows with a NVIDIA GPU, you may need to set up the app using the NVIDIA Control Panel. Here’s a step-by-step guide:

1. **Download and Install Ollama:**
   - Use the [Ollama installer](https://ollama.com/download) for Windows.

   > **Tip: Set Up GPU Usage via NVIDIA Control Panel**
   > - Open the **NVIDIA Control Panel**.
   > - Go to **Manage 3D Settings** > **Program Settings**.
   > - Add the **Ollama** app from your installation directory.
   > - Under **Select the preferred graphics processor**, choose **High-performance NVIDIA processor**.
   > - Apply the changes.

2. **Run Ollama:**
   - Once configured, you can run Ollama commands in your terminal, and it will leverage your NVIDIA GPU automatically.

Make sure your GPU drivers are up-to-date for optimal performance.

### 1.2.0 Set Up LocalAI

#### 1.2.1 Install Docker

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

#### 1.2.2 Check Your CUDA Version (Optional, for GPU Support)

If you plan to use LocalAI with NVIDIA GPUs, you'll need to check which CUDA version is installed on your system.

- Run the following command to check your CUDA version:

```bash
nvidia-smi
```

Look for the "CUDA Version" at the top of the output.

#### 1.2.3 Set up LocalAI

Next, run LocalAI using Docker. The command depends on whether you have an NVIDIA, Intel, or AMD GPU, and which CUDA version you have.

**For systems without GPUs (CPU only):**

```bash
docker run -p 8080:8080 --name local-ai -ti localai/localai:latest-cpu
```

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

#### 1.2.4 Download a Model

After starting the LocalAI container, you can download a model via the web interface.

1. **Open Your Web Browser** and go to:

   ```
   http://localhost:8080/browse/
   ```

2. **Browse and Select the Model**:
   - Navigate through the available models (e.g., find `meta-llama-3.1-8b-instruct`).
   - Click on the model name or corresponding download button to start the download.

3. **Wait for the Download to Complete**:
   - The download progress will be displayed on the web page.
   - Ensure the download is fully completed before closing the browser or stopping the Docker container.

## 2. Examples

### 2.1 A Simple Example Using Ollama

Here’s how you can use Supachain with Ollama to determine if a statement is true or false:

```kotlin
import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Answer
import dev.supachain.robot.provider.models.Ollama

private interface TrueOrFalse {
    fun chat(prompt: String): Answer<Boolean>
}

fun main() {
    val robot = Robot<Ollama, TrueOrFalse, NoTools>()
    val answer = robot.chat("The tallest building in the world is taller than Mt. Everest").await()
    println(answer)
}
```

#### What’s Going On?

1. **Define an interface:** The `TrueOrFalse` interface specifies the expected format of the answer (a boolean value).
2. **Create a Robot instance:** The `Robot` instance is configured to use Ollama as the provider and expects `TrueOrFalse` answers.
3. **Ask a question:** The `chat` method is used to ask the question.
4. **Get the answer:** The `await()` method waits for the answer and returns it.

### 2.2 Customizing Your Local Provider

Configure Supachain to use a local AI provider with a specific URL and chat model:

```kotlin
import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Chat
import dev.supachain.robot.provider.models.LocalAI

fun main() {
    val robot = Robot<LocalAI, Chat, NoTools> {
        defaultProvider {
            url = "http://localhost:8888"
            chatModel = "meta-llama-3.1-8b-instruct"
        }
    }

    val answer = robot.chat("Whats 11 * 7 + 1").await()
    println(answer)
}
```

#### What’s Going On?

1. **Robot with Configuration:** A `Robot` instance is created with `LocalAI` as the provider and a specific model.
2. **Customizing Provider:** The `defaultProvider` block allows for configuration of the

 provider URL and the specific model.
3. **Ask a question:** The `chat` method asks a math question.
4. **Get the answer:** The answer is awaited and printed.

### 2.3 Days of the Week Example with LocalAI

This example checks if “Friday” is a day of the week using a local model:

```kotlin
import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Answer
import dev.supachain.robot.provider.models.LocalAI

private interface DaysOfWeek {
    fun checkDay(day: String): Answer<Boolean>
}

fun main() {
    val robot = Robot<LocalAI, DaysOfWeek, NoTools>()
    val answer = robot.checkDay("Friday").await()
    println(answer)
}
```

#### What’s Going On?

1. **Define an interface:** `DaysOfWeek` is the expected interface for checking days.
2. **Create a Robot instance:** A `Robot` instance is configured to use `LocalAI`.
3. **Check the day:** The `checkDay` method checks if “Friday” is a day of the week.
4. **Get the answer:** The answer is awaited and printed.

### 2.4 Using Simple Math Operations in Supachain

Supachain allows you to perform basic arithmetic operations with AI:

```kotlin
import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.MathOperation
import dev.supachain.robot.provider.models.LocalAI

private interface Calculator {
    fun calculate(expression: String): MathOperation
}

fun main() {
    val robot = Robot<LocalAI, Calculator, NoTools>()
    val result = robot.calculate("11 + 22 * 3 - 10 / 2").await()
    println(result)
}
```

#### What’s Going On?

1. **Define an interface:** `Calculator` is an interface for simple math operations.
2. **Create a Robot instance:** A `Robot` instance is configured to use `LocalAI` as the provider.
3. **Perform a calculation:** The `calculate` method is used to evaluate a math expression.
4. **Get the result:** The result is awaited and printed.

### 2.5 Using Tools in Supachain with LocalAI

In Supachain, you can utilize various tools to enhance the AI’s capabilities:

```kotlin
import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Answer
import dev.supachain.robot.provider.models.LocalAI

private interface ToolUser {
    fun useTool(input: String): Answer<String>
}

fun main() {
    val robot = Robot<LocalAI, ToolUser, NoTools>()
    val result = robot.useTool("Tell me a joke about AI.").await()
    println(result)
}
```

#### What’s Going On?

1. **Define an interface:** `ToolUser` is an interface for using a specific tool.
2. **Create a Robot instance:** A `Robot` instance is configured to use `LocalAI` as the provider.
3. **Use a tool:** The `useTool` method is used to get an AI-generated joke.
4. **Get the result:** The result is awaited and printed.

---

This guide provides an overview of setting up and using Supachain with various local AI providers, including Ollama and LocalAI. The examples illustrate how to configure providers, perform basic operations, and use Supachain’s capabilities to build AI-driven applications.

## 3.0 Mixture of Concepts

In Supachain, a "Mixture of Concepts" (MoC) allows you to combine different AI-driven tasks into a single cohesive process. This approach is similar to the "Mixture of Experts" (MoE) model but simplifies the setup by requiring only one agent to handle various tasks. This single-agent model leverages Supachain’s modular design to perform complex workflows efficiently.

### 3.1 Building a Pro's and Con's System

You can create a system to evaluate the pros and cons of a specific issue using Supachain’s concept chaining capabilities. This system allows you to combine different AI models to generate a comprehensive answer. Here's an example of how to implement such a system:

```kotlin
fun main() {
    Debug show "Messenger"
    val robot = Robot<Ollama, Chat, NoTools>()

    val pros = concept { "Show me all the pros for this issue: $inputted" }
    val cons = concept { "Show me all the cons for this issue: $inputted" }

    val summary = mix {
        "Show the pros and cons and weigh out the answers. " +
                "If you had a third holistic perspective share it. Pros/Cons: $inputted"
    }

    val answer = pros + cons to summary

    val question = "Should I own a macbook?"

    val generatedAnswer = (answer using { robot.chat(it).await() }).produce(question)

    println(generatedAnswer)
}
```

#### Explanation

1. **Define Concepts:**
   - `pros` and `cons` are concepts that gather the pros and cons for a given issue, respectively. They format the prompt accordingly.

2. **Create Summary:**
   - `summary` combines the results from the `pros` and `cons` concepts and adds a holistic perspective.

3. **Generate Answer:**
   - Combine `pros` and `cons` to form the final answer using the `summary` concept.

4. **Ask a Question:**
   - The `question` variable specifies the issue at hand.

5. **Produce Answer:**
   - Use the `answer` combined with `robot.chat()` to generate a response based on the question.

This example demonstrates how to integrate multiple AI-driven concepts into a single workflow to analyze and respond to complex questions.
