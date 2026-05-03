module.exports = {
  pdf_options: {
    format: 'A4',
    margin: {
      top: '20mm',
      bottom: '20mm',
      left: '15mm',
      right: '15mm'
    },
    printBackground: true
  },
  launch_options: {
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  },
  stylesheet: [],
  body_class: [],
  script: [
    { url: 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js' },
    { content: `
      document.addEventListener('DOMContentLoaded', function() {
        mermaid.initialize({ startOnLoad: false, theme: 'default' });
        document.querySelectorAll('pre code.language-mermaid').forEach(function(codeBlock) {
          var pre = codeBlock.parentNode;
          var div = document.createElement('div');
          div.className = 'mermaid';
          div.textContent = codeBlock.textContent;
          pre.parentNode.replaceChild(div, pre);
        });
        mermaid.init(undefined, document.querySelectorAll('.mermaid'));
      });
    `}
  ],
  css: `
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
      line-height: 1.6;
    }
    h1, h2, h3, h4, h5, h6 {
      color: #2c3e50;
      margin-top: 1.5em;
    }
    h1 { border-bottom: 2px solid #3498db; padding-bottom: 10px; }
    h2 { border-bottom: 1px solid #bdc3c7; padding-bottom: 8px; }
    pre {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 5px;
      border: 1px solid #e9ecef;
      overflow-x: auto;
    }
    code {
      background: #f1f3f5;
      padding: 2px 6px;
      border-radius: 3px;
      font-size: 0.9em;
    }
    pre code {
      background: none;
      padding: 0;
    }
    table {
      border-collapse: collapse;
      width: 100%;
      margin: 15px 0;
      font-size: 0.9em;
    }
    th, td {
      border: 1px solid #dee2e6;
      padding: 10px 12px;
      text-align: left;
    }
    th {
      background-color: #f8f9fa;
      font-weight: 600;
    }
    tr:nth-child(even) {
      background-color: #f8f9fa;
    }
    blockquote {
      border-left: 4px solid #3498db;
      margin: 15px 0;
      padding: 10px 20px;
      background: #f8f9fa;
      color: #555;
    }
    hr {
      border: none;
      border-top: 1px solid #dee2e6;
      margin: 30px 0;
    }
    .mermaid {
      text-align: center;
      margin: 20px 0;
    }
  `,
  document_title: 'DVP - JakeBooks',
  highlight_style: 'github'
};
