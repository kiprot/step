// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */

function randomizeImage() {
  // Images directory contains 8 images
  const imageIndex = String(Math.floor(Math.random() * 8) + 1);
  const imgUrl = 'images/dino_' + imageIndex + '.jpg';

  const imgElement = document.createElement('img');
  imgElement.src = imgUrl;

  const imageContainer = document.getElementById('dinosaur-image-container');
  // Remove the previous image.
  imageContainer.innerHTML = '';
  imageContainer.appendChild(imgElement);
}

function toggleDetailsButton() {
  var x = document.getElementById("details");
  if (x.style.display === "none") {
    x.style.display = "block";
  } else {
    x.style.display = "none";
  }
}

// gets the data from server sides
function getServerData() {
    fetch('/data')  
    .then(response => response.json()) 
    .then((data) => {
        const dataListElement = document.getElementById('comments');
        dataListElement.innerHTML = '';
        //for(var i = 0; i < data.length; i++) {
        for(let i in data) {
            dataListElement.appendChild(createListElement(data[i]));
            console.log(data[i]);
        }
    });
}

// creates a list element with text
function createListElement(text) {
    const liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
}