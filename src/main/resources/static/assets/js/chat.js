document.addEventListener("DOMContentLoaded", function () {
  const chatToggle = document.getElementById("chatToggle");
  const chatWidget = document.getElementById("chatWidget");
  const messageInput = document.getElementById("messageInput");
  const sendButton = document.getElementById("sendButton");
  const chatMessages = document.getElementById("chatMessages");
  const typingIndicator = document.getElementById("typingIndicator");
  const customerSessionId = document.getElementById("customerSessionId");

  let isRetrying = false;
  const MAX_RETRIES = 3;
  const RETRY_DELAY = 1000; // 1 second
  const CHATBOT_URL = "https://sinhdang.app.n8n.cloud/webhook/chatbot";
  let hasShownWelcome = false;

  // Toggle chat widget with animation
  chatToggle.addEventListener("click", function () {
    // Remove 'hidden' class to show the widget
    chatWidget.classList.toggle("hidden");

    // If widget is now visible, focus on input and show welcome message
    if (!chatWidget.classList.contains("hidden")) {
      messageInput.focus();
      // Add animation class
      chatWidget.style.transform = "scale(1)";
      chatWidget.style.opacity = "1";

      // Show welcome message if not shown before
      if (!hasShownWelcome) {
        showWelcomeMessage();
        hasShownWelcome = true;
      }
    } else {
      // Add animation for hiding
      chatWidget.style.transform = "scale(0)";
      chatWidget.style.opacity = "0";
    }
  });

  function showWelcomeMessage() {
    // Show typing indicator first
    showTypingIndicator();

    // Add welcome messages with delays for natural feel
    setTimeout(() => {
      hideTypingIndicator();
      addMessage(
        "Tôi là Trợ lý Bán hàng của cửa hàng. Bạn đang tìm kiếm sản phẩm nào hay cần tư vấn về vấn đề gì ạ?",
        "agent"
      );
    }, 1000);
  }

  // Send message when clicking send button
  sendButton.addEventListener("click", handleSendMessage);

  // Send message when pressing Enter
  messageInput.addEventListener("keypress", function (e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  });

  async function handleSendMessage() {
    const message = messageInput.value.trim();
    if (message === "") return;

    // Get session ID from hidden input
    const sessionId = customerSessionId ? customerSessionId.value : null;
    if (!sessionId) {
      addErrorMessage("Please log in to use the chat.");
      return;
    }

    // Disable input and button while sending
    setInputState(false);

    // Add message to chat
    addMessage(message, "customer");

    // Show typing indicator
    showTypingIndicator();

    try {
      const response = await sendMessageWithRetry(message, sessionId);
      hideTypingIndicator();

      if (response && response.message) {
        // Add a small delay to make it feel more natural
        setTimeout(() => {
          addMessage(response.message, "agent");
        }, 500);
      }
    } catch (error) {
      console.error("Error:", error);
      hideTypingIndicator();
      addErrorMessage("Failed to send message. Please try again.");
    } finally {
      // Re-enable input and button
      setInputState(true);
      messageInput.value = "";
    }
  }

  async function sendMessageWithRetry(message, sessionId, retryCount = 0) {
    try {
      const response = await fetch(CHATBOT_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          message: message,
          session: sessionId,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();

      // Return in the expected format for the chat interface
      return {
        message: data.output || "Sorry, I couldn't process your message.",
      };
    } catch (error) {
      if (retryCount < MAX_RETRIES) {
        // Show retry message
        updateTypingIndicator(`Retrying... (${retryCount + 1}/${MAX_RETRIES})`);

        // Wait before retrying
        await new Promise((resolve) =>
          setTimeout(resolve, RETRY_DELAY * (retryCount + 1))
        );

        // Retry
        return sendMessageWithRetry(message, sessionId, retryCount + 1);
      }
      throw error;
    }
  }

  // Enhanced function to format AI messages with better text processing
  function formatAIMessage(text) {
    if (!text) return "";

    // Clean up extra whitespace and normalize line breaks
    text = text.trim().replace(/\r\n/g, "\n").replace(/\r/g, "\n");

    // Handle special case where the entire message is one continuous block
    // Split on multiple patterns for better paragraph detection
    let paragraphs;
    if (text.includes("\n\n")) {
      paragraphs = text.split(/\n\s*\n/);
    } else {
      // If no double line breaks, try to split on logical breaks
      paragraphs = text.split(/(?:\n(?=[-•])|(?<=\?|\.|!)\s*\n)/);
    }

    const formattedParagraphs = paragraphs.map((paragraph, index) => {
      // Clean up the paragraph
      paragraph = paragraph.trim();
      if (!paragraph) return "";

      // Handle different types of content
      if (paragraph.includes("- ") || paragraph.includes("• ")) {
        // This is a list - format as HTML list
        return formatList(paragraph);
      } else if (paragraph.match(/^\d+\./)) {
        // This is a numbered list
        return formatNumberedList(paragraph);
      } else {
        // Regular paragraph - format inline elements and wrap in p tag
        const formatted = formatInlineElements(paragraph);
        return `<p>${formatted}</p>`;
      }
    });

    return formattedParagraphs.filter((p) => p).join("");
  }

  function formatList(text) {
    const items = text
      .split(/\n/)
      .map((line) => {
        line = line.trim();
        if (line.startsWith("- ")) {
          return line.substring(2).trim();
        } else if (line.startsWith("• ")) {
          return line.substring(2).trim();
        }
        return line;
      })
      .filter((item) => item);

    if (items.length === 0) return "";

    const listItems = items
      .map((item) => `<li>${formatInlineElements(item)}</li>`)
      .join("");
    return `<ul class="chat-list">${listItems}</ul>`;
  }

  function formatNumberedList(text) {
    const items = text
      .split(/\n/)
      .map((line) => {
        line = line.trim();
        const match = line.match(/^\d+\.\s*(.+)/);
        return match ? match[1].trim() : line;
      })
      .filter((item) => item);

    if (items.length === 0) return "";

    const listItems = items
      .map((item) => `<li>${formatInlineElements(item)}</li>`)
      .join("");
    return `<ol class="chat-list">${listItems}</ol>`;
  }

  function formatInlineElements(text) {
    if (!text) return "";

    // Format bold text (**text** or __text__)
    text = text.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
    text = text.replace(/__(.*?)__/g, "<strong>$1</strong>");

    // Format italic text (*text* or _text_)
    text = text.replace(/\*(.*?)\*/g, "<em>$1</em>");
    text = text.replace(/_(.*?)_/g, "<em>$1</em>");

    // Handle line breaks within paragraphs
    text = text.replace(/\n/g, "<br>");

    // Highlight prices (Vietnamese format)
    text = text.replace(
      /(\d{1,3}(?:\.\d{3})*(?:\,\d{2})?\s*(?:VND|đ|vnđ))/gi,
      '<span class="price-highlight">$1</span>'
    );

    // Highlight product names in quotes
    text = text.replace(
      /"([^"]+)"/g,
      '<span class="product-highlight">"$1"</span>'
    );

    // Highlight questions (text ending with question marks)
    text = text.replace(
      /([^<>]*\?)/g,
      '<span class="question-highlight">$1</span>'
    );

    return text;
  }

  function addMessage(message, sender) {
    const messageElement = document.createElement("div");
    messageElement.classList.add("message", sender);

    // Apply enhanced formatting for agent messages
    if (sender === "agent") {
      const formattedMessage = formatAIMessage(message);
      messageElement.innerHTML = formattedMessage;
    } else {
      // Keep customer messages as plain text
      messageElement.textContent = message;
    }

    chatMessages.appendChild(messageElement);

    // Scroll to bottom
    scrollToBottom();
  }

  function addErrorMessage(message) {
    const errorElement = document.createElement("div");
    errorElement.classList.add("message", "error");
    errorElement.textContent = message;
    chatMessages.appendChild(errorElement);

    // Remove error message after 5 seconds
    setTimeout(() => {
      errorElement.style.animation = "fadeOut 0.3s ease-in-out";
      setTimeout(() => errorElement.remove(), 300);
    }, 5000);

    scrollToBottom();
  }

  function scrollToBottom() {
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function showTypingIndicator() {
    typingIndicator.classList.add("visible");
  }

  function hideTypingIndicator() {
    typingIndicator.classList.remove("visible");
  }

  function updateTypingIndicator(message) {
    const textNode = typingIndicator.childNodes[0];
    textNode.nodeValue = message + " ";
  }

  function setInputState(enabled) {
    messageInput.disabled = !enabled;
    sendButton.disabled = !enabled;
    if (enabled) {
      sendButton.textContent = "Send";
    } else {
      sendButton.textContent = "Sending...";
    }
  }

  // Initialize chat widget state
  chatWidget.style.transform = "scale(0)";
  chatWidget.style.opacity = "0";
});
