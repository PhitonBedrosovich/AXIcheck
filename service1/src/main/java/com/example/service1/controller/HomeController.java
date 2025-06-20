package com.example.service1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
            <html>
                <head>
                    <title>Batch Processing Service</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; }
                        h1 { color: #333; }
                        .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-radius: 4px; }
                        code { background: #eee; padding: 2px 4px; border-radius: 3px; }
                        .upload-form { 
                            background: #f5f5f5; 
                            padding: 20px; 
                            margin: 20px 0; 
                            border-radius: 4px;
                            border: 1px solid #ddd;
                        }
                        .upload-form input[type="file"] {
                            margin: 10px 0;
                        }
                        .upload-form input[type="submit"] {
                            background: #4CAF50;
                            color: white;
                            padding: 10px 20px;
                            border: none;
                            border-radius: 4px;
                            cursor: pointer;
                        }
                        .upload-form input[type="submit"]:hover {
                            background: #45a049;
                        }
                    </style>
                </head>
                <body>
                    <h1>Batch Processing Service API</h1>
                    <p>Welcome to the Batch Processing Service. Here are the available endpoints:</p>
                    
                    <div class="upload-form">
                        <h3>Upload Batch File</h3>
                        <form action="/api/batch/upload" method="post" enctype="multipart/form-data">
                            <input type="file" name="file" accept=".zip" required>
                            <br>
                            <input type="submit" value="Upload">
                        </form>
                    </div>
                    
                    <div class="endpoint">
                        <h3>Upload Batch</h3>
                        <p><strong>POST</strong> <code>/api/batch/upload</code></p>
                        <p>Upload a ZIP file containing XML requests for batch processing.</p>
                    </div>
                    
                    <div class="endpoint">
                        <h3>Get Batch Status</h3>
                        <p><strong>GET</strong> <code>/api/batch/status/{batchId}</code></p>
                        <p>Get the status of a batch processing request.</p>
                    </div>
                </body>
            </html>
            """;
    }
} 