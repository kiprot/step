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

/** Displays a random image */
function randomizeImage() {
  // Images directory contains 8 images.
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

/** Gets the data from server sides. */
function loadComments(value) {
    fetch('/data?num=' + value)  
    .then(response => response.json()) 
    .then((data) => {
        createCommentElement(data, 'comments');
    });
}

/** Creates an element for displaying comments. */
function createCommentElement(data, attribute) {
    const dataListElement = document.getElementById(attribute);
    dataListElement.innerHTML = '';
    for(let i in data) {
        const commentElement = document.createElement('li');
        commentElement.className = 'comment';

        const titleElement = document.createElement('span');
        titleElement.innerText = data[i].comment;

        const deleteButtonElement = document.createElement('button');
        deleteButtonElement.innerText = 'Delete';
        deleteButtonElement.addEventListener('click', () => {
            deleteComment(data[i]);
            // Remove the task from the DOM.
            commentElement.remove();
        });

        commentElement.appendChild(titleElement);
        commentElement.appendChild(deleteButtonElement);
        dataListElement.appendChild(commentElement);
    }
}

/** Tells the server to delete the comment in the datastore. */
function deleteComment(comment) {
    const params = new URLSearchParams();
    params.append('id', comment.id);
    fetch('/delete-data', {method: 'POST', body: params});
}