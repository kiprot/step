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
    fetch('/data?num=' + value.toString())  
    .then(response => response.json()) 
    .then((data) => {
        createCommentElement(data, 'comments');
    });
}

/* Creates and returns an image element for displaying. */
function addImage(src) {
  const image = document.createElement('img');
  image.setAttribute("src", src);
  image.setAttribute("max-width", "304");
  image.setAttribute("height", "auto");
  image.setAttribute("alt", "User's Uploaded Image");
  return image;
}

/** Creates an element for displaying comments. */
function createCommentElement(data, attribute) {
  const dataListElement = document.getElementById(attribute);
  dataListElement.innerHTML = '';
    for(let i in data) {
      const commentElement = document.createElement('dl');
      const commentElementImage = document.createElement('dt');
      const commentElementTitle = document.createElement('dt');
      const commentElementDelete = document.createElement('dt');
      commentElement.className = 'comment';

      const deleteButtonElement = document.createElement('button');
      deleteButtonElement.innerText = 'Delete';
      deleteButtonElement.addEventListener('click', () => {
        deleteComment(data[i]);
        // Remove the task from the DOM.
        commentElement.remove();
      });

      commentElementTitle.innerText = data[i].comment;
      commentElementDelete.appendChild(deleteButtonElement);
      commentElementImage.appendChild(addImage(data[i].imageUrl));

      commentElement.appendChild(commentElementImage)
      .appendChild(commentElementTitle)
      .appendChild(commentElementDelete);

      dataListElement.appendChild(commentElement);
    }
}

/** Tells the server to delete the comment in the datastore. */
function deleteComment(comment) {
    const params = new URLSearchParams();
    params.append('id', comment.id);
    fetch('/delete-data', {method: 'POST', body: params});
}

/** Gets BlobStore url from the server. */
function fetchBlobstoreUrlAndShowForm() {
    fetch('/blobstore-upload-url')
    .then((response) => {
        return response.text();
    })
    .then((imageUploadUrl) => {
        const messageForm = document.getElementById('my-form');
        messageForm.action = imageUploadUrl;
        messageForm.classList.remove('hidden');
    });
}

var museums = [
    ['AMNH', 40.781470, -73.974020, 1],
    ['The Met', 40.779632, -73.963266, 2],
    ['Guggenheim', 40.783085, -73.958981, 3],
    ['MoMA', 40.761579, -73.977675, 4],
    ['Whitney Museum', 40.739815, -74.008874, 5],
    ['Cloisters', 40.865041, -73.931727, 6],
    ['The Intrepid', 40.865041, -73.931727, 7],
    ['Museum of Sex', 40.744277, -73.987454, 8],
    ['9/11 Memorial', 40.711582, -74.013851, 9]
]

/** Puts markers on some specified map locations. */
function setMarkers(map) {
    for (var i = 0; i < museums.length; i++) {
        var museum = museums[i];
        var marker = new google.maps.Marker({
            position: {lat: museum[1], lng: museum[2]},
            map: map,
            title: museum[0],
            // Takes care of the layering levels on the map.
            zIndex: museum[3]
        });
    }
}

/** Creates a map centered in Manhattan NY. */
function createMap() {
    const map = new google.maps.Map(
        document.getElementById('map'),
        {center: {lat: 40.7831, lng: -73.9712} , zoom: 12});

    setMarkers(map);
}

/** Calls 2 other functions for onloading purposes. */
function onloadFunc() {
    fetchBlobstoreUrlAndShowForm();
    createMap();
}