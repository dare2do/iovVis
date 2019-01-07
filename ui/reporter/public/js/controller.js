/* eslint-disable no-console */

var progressbar = $("#progressbar");
max = progressbar.attr('max');
value = progressbar.val();
time = (1000 / max) * 5;

var loading = function () {
  value += 1;
  addValue = progressbar.val(value);
  $(".progress_value").html(value + "%");
  if (value == max)
    value = 0;
};

var animate;

var bar = $(".html5-progressbar");
console.log('Starting swapi demo');

const createReport = docxTemplates; // eslint-disable-line

// callback when a template has been selected
async function onTemplateChosen(event) {
  value = 0;
  bar.show();
  animate = setInterval(function () {
    loading();
  }, time);

  let flag = false;
  console.log('Template chosen');
  // read the file in an ArrayBuffer
  const content = await readFile(this.files[0]);
  // select the right data source, depending on selected input
  // let data = null;
  // if (event.target.id === 'inputSwapi') {
  //   data = query => postQuery('/swapi', query); // query to swapi webservice
  // } else if (event.target.id === 'inputQuill') {
  //   data = { html: `<body>${window.quill.root.innerHTML}</body>` };
  // }
  // fill the template
  console.log('Creating report (can take some time) ...');
  const doc = await createReport({
    template: content, data: { name: 'Awen', createTime: new Date().toISOString().replace(/T/, ' ').replace(/\..+/, '') },
    additionalJsContext: {
      genIMG: async (type, tsw, w_cm, h_cm) => {
        console.log('--- try to axios ---', type, tsw);
        let data = [];
        await axios.post('/swapi', {
          // query: ' {allFilms { films { title, releaseDate } } }',
          chartType: type,
          chartTsw: tsw,
          chartW: w_cm,
          chartH: h_cm
        })
          .then(function (response) {
            console.log(response);
            const dataUrl = response.data;
            data = dataUrl.slice('data:image/png;base64,'.length);
          })
          .catch(function (error) {
            console.log(error);
          });
        flag = true;
        return { width: w_cm, height: h_cm, data, extension: '.png' };
      }
    }
  }).catch(err => {
    console.log(err);
    bar.show();
    clearInterval(animate);
  });
  // generate output file for download
  if (flag) {
    downloadBlob(
      doc,
      'report.docx',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    );
    bar.hide();
    clearInterval(animate);
  }
}

// ==============================================
//                 Helpers
// ==============================================

// add an event listener on file-input change
// (need to wait for DOM to be loaded otherwise input will be undefined)
document.addEventListener('DOMContentLoaded', () => {
  let inputElement = document.getElementById('inputSwapi');
  inputElement.addEventListener('change', onTemplateChosen, false);
});

// send query to swapi: we need to forward query to server to
// avoid browser's Cross-Site Requests blocker
async function postQuery(url, query) {
  return new Promise(resolve => {
    // Sending and receiving data in JSON format using POST method
    const xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-type', 'application/json');
    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4 && xhr.status === 200) {
        const json = JSON.parse(xhr.responseText);
        resolve(json);
      }
    };
    const data = JSON.stringify({ query });
    xhr.send(data);
  });
}

// read given file into an ArrayBuffer
async function readFile(fd) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = reject;
    reader.onload = () => {
      resolve(reader.result);
    };
    reader.readAsArrayBuffer(fd);
  });
}

// helper to download data as a file (like saveAs)
function downloadURL(data, fileName) {
  const a = document.createElement('a');
  a.href = data;
  a.download = fileName;
  document.body.appendChild(a);
  a.style = 'display: none';
  a.click();
  a.remove();
}

function downloadBlob(data, fileName, mimeType) {
  const blob = new Blob([data], {
    type: mimeType,
  });
  const url = window.URL.createObjectURL(blob);
  downloadURL(url, fileName, mimeType);
  setTimeout(() => {
    window.URL.revokeObjectURL(url);
  }, 1000);
}
