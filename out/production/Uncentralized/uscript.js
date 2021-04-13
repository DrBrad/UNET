var options = document.getElementById('options');
var overlay = document.getElementById('overlay');
var deleteForm = document.getElementById('deleteForm');
var changeVisForm = document.getElementById('changeVisForm');
var hiddenFrame = document.getElementById('hiddenFrame');
var warning = document.getElementById('warning');
var frameNames = [];
var selectedEle = null;
var table = document.getElementsByTagName('tbody')[0];
var selected = document.querySelectorAll('th')[0];
toggle = false;

var x = 0, y = 0;
window.onmousemove = function(event){
    x = event.clientX;
    y = event.clientY;
}

var pictures = [
    'png',
    'jpg',
    'jpeg',
    'gif',
    'svg'
];

var videos = [
    'mp4',
    'mkv',
    'webm',
    'flv',
    'avi',
    '3gp',
    'mov',
    'vob',
    'wmv',
    'ogv',
    'm4v',
    'm4p',
    'mng',
    'qt',
    'mvb',
    'mpg',
    'mmpv',
    'm2v',
    'mp2',
    'mmpv',
    'm2v',
    'svi',
    '32g'
];

var audios = [
    'mp3',
    'ogg'
];

var archives = [
    'zip',
    'tar.gz',
    'tar',
    'rar',
    '7zp'
];

//DONE
function uploadFile(ele){
    if(frameNames.length < 4){
        var fileEles = [];
        var ran = true;

        var files = ele.files;
        if(files.length < 4){
            for(var i = 0; i < files.length; i++){
                var fileName = files[i].name.split(/(\\|\/)/g).pop();
                var extension = fileName.split('.').pop();

                if(fileName.length < 40){
                    var nfile = document.createElement('tr');
                    nfile.setAttribute('key', fileName);
                    nfile.setAttribute('ktype', 'f');
                    nfile.className = 'load';
                    nfile.oncontextmenu = function(event){
                        optionsContext(event, nfile);
                    }

                    if(pictures.indexOf(extension) > -1){
                        nfile.innerHTML = "<td class='i'><a href='http://"+domain+".unet/"+query+fileName+"' target='_blank'>"+fileName+"</a></td><td>Image</td><td>Public</td>";
                    }else if(videos.indexOf(extension) > -1){
                        nfile.innerHTML = "<td class='v'><a href='http://"+domain+".unet/"+query+fileName+"' target='_blank'>"+fileName+"</a></td><td>Video</td><td>Public</td>";
                    }else if(audios.indexOf(extension) > -1){
                        nfile.innerHTML = "<td class='a'><a href='http://"+domain+".unet/"+query+fileName+"' target='_blank'>"+fileName+"</a></td><td>Audio</td><td>Public</td>";
                    }else if(archives.indexOf(extension) > -1){
                        nfile.innerHTML = "<td class='z'><a href='http://"+domain+".unet/"+query+fileName+"' target='_blank'>"+fileName+"</a></td><td>Archive</td><td>Public</td>";
                    }else{
                        nfile.innerHTML = "<td class='d'><a href='http://"+domain+".unet/"+query+fileName+"' target='_blank'>"+fileName+"</a></td><td>Document</td><td>Public</td>";
                    }

                    table.appendChild(nfile);
                    fileEles.push(nfile);
                }else{
                    ran = false;
                    break;
                }
            }

            if(ran){
                var frameName = randomName();

                var nframe = document.createElement('iframe');
                nframe.name = frameName;
                nframe.onload = function(event){
                    document.body.removeChild(nframe);
                    frameNames.splice(frameNames.indexOf(frameName), 1);

                    for(var i = 0; i < fileEles.length; i++){
                        fileEles[i].className = '';
                    }
                }

                nframe.onerror = function(event){
                    document.body.removeChild(nframe);
                    frameNames.splice(frameNames.indexOf(frameName), 1);

                    for(var i = 0; i < fileEles.length; i++){
                        table.removeChild(fileEles[i]);
                    }
                }
                document.body.appendChild(nframe);

                ele.parentElement.target = frameName;
                ele.parentElement.submit();

            }else{
                for(var i = 0; i < fileEles.length; i++){
                    table.removeChild(fileEles[i]);
                }

                warning.innerHTML = "<span style='font-size: 20px'>Warning!</span><br><br>File names must be less than 40 chars.";
                warning.style.display = 'block';
                setTimeout(function(){ warning.style.display = 'none'; }, 4000);
            }
        }else{
            warning.innerHTML = "<span style='font-size: 20px'>Warning!</span><br><br>You can only upload up to 3 files at a time.";
            warning.style.display = 'block';
            setTimeout(function(){ warning.style.display = 'none'; }, 4000);
        }
    }else{
        warning.innerHTML = "<span style='font-size: 20px'>Warning!</span><br><br>Please wait for one of the tasks to finish.<br>You can only do 4 tasks at once.";
        warning.style.display = 'block';
        setTimeout(function(){ warning.style.display = 'none'; }, 4000);
    }
}

//DONE
function newFolder(ele){
    if(frameNames.length < 4){
        document.getElementById("cdirBool").checked = false;

        var ndir = document.createElement('tr');
        ndir.setAttribute('key', ele.childNodes[3].value);
        ndir.setAttribute('ktype', 'd');
        ndir.className = 'load';
        ndir.oncontextmenu = function(event){
            optionsContext(event, ndir);
        }

        ndir.innerHTML = "<td class='f'><a href='/upload?q="+query+ele.childNodes[3].value+"/'>"+ele.childNodes[3].value+"</a></td><td>Folder</td><td></td>";

        table.appendChild(ndir);

        var frameName = randomName();

        var nframe = document.createElement('iframe');
        nframe.name = frameName;
        nframe.onload = function(event){
            ndir.className = '';
            document.body.removeChild(nframe);
            frameNames.splice(frameNames.indexOf(frameName), 1);
        }

        nframe.onerror = function(event){
            table.removeChild(ndir);
            document.body.removeChild(nframe);
            frameNames.splice(frameNames.indexOf(frameName), 1);
        }
        document.body.appendChild(nframe);

        ele.target = frameName;
        ele.submit();
        ele.childNodes[3].value = '';
    }else{
        warning.innerHTML = "<span style='font-size: 20px'>Warning!</span><br><br>Please wait for one of the tasks to finish.<br>You can only do 4 tasks at once.";
        warning.style.display = 'block';
        setTimeout(function(){ warning.style.display = 'none'; }, 4000);
    }
}

//DONE
function deleteAny(ele){
    if(frameNames.length < 4){
        overlay.style.display = 'none';
        options.style.display = 'none';

        var key = deleteForm.childNodes[1].value;
        var child = document.querySelector('tr[key="'+key+'"]');
        child.className = 'load';

        var frameName = randomName();

        var nframe = document.createElement('iframe');
        nframe.name = frameName;
        nframe.onload = function(event){
            child.parentElement.removeChild(child);
            document.body.removeChild(nframe);
            frameNames.splice(frameNames.indexOf(frameName), 1);
        }

        nframe.onerror = function(event){
            child.firstElementChild.className = '';
            document.body.removeChild(nframe);
            frameNames.splice(frameNames.indexOf(frameName), 1);
        }
        document.body.appendChild(nframe);

        ele.target = frameName;
        ele.submit();
    }else{
        warning.innerHTML = "<span style='font-size: 20px'>Warning!</span><br><br>Please wait for one of the tasks to finish.<br>You can only do 4 tasks at once.";
        warning.style.display = 'block';
        setTimeout(function(){ warning.style.display = 'none'; }, 4000);
    }
}

function changeVisibility(ele){
    if(frameNames.length < 4){
        overlay.style.display = 'none';
        options.style.display = 'none';

        var key = deleteForm.childNodes[1].value;
        var child = document.querySelector('tr[key="'+key+'"]');
        child.style.backgroundColor = '';
        child.className = 'load';

        var frameName = randomName();

        var nframe = document.createElement('iframe');
        nframe.name = frameName;
        nframe.onload = function(event){
            child.className = '';
            if(child.childNodes[2].innerHTML == 'Private'){
                child.childNodes[2].className = '';
                child.childNodes[2].innerHTML = 'Public';
            }else{
                child.childNodes[2].className = 'p';
                child.childNodes[2].innerHTML = 'Private';
            }
            document.body.removeChild(nframe);
            frameNames.splice(frameNames.indexOf(frameName), 1);
        }

        nframe.onerror = function(event){
            child.firstElementChild.className = '';
            document.body.removeChild(nframe);
            frameNames.splice(frameNames.indexOf(frameName), 1);
        }
        document.body.appendChild(nframe);

        ele.target = frameName;
        ele.submit();
    }else{
        warning.innerHTML = "<span style='font-size: 20px'>Warning!</span><br><br>Please wait for one of the tasks to finish.<br>You can only do 4 tasks at once.";
        warning.style.display = 'block';
        setTimeout(function(){ warning.style.display = 'none'; }, 4000);
    }
}

function randomName(){
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', builder = '';
    for(var i = 0; i < 10; i++){
        builder += characters.charAt(Math.floor(Math.random()*characters.length));
    }

    if(frameNames.indexOf(builder) > -1){
        return randomName();
    }else{
        frameNames.push(builder);
        return builder;
    }
}

var x = 0, y = 0;
window.onmousemove = function(event){
    x = event.clientX;
    y = event.clientY;
}

function optionsContext(event, ele){
    selectedEle = ele;
    overlay.style.display = 'block';
    options.style.display = 'block';
    options.style.top = y+'px';

    if(window.innerWidth-300 > x){
        options.style.right = '';
        options.style.left = x+'px';
    }else{
        options.style.left = '';
        options.style.right = window.innerWidth-x+'px';
    }

    if(ele.getAttribute('ktype') == 'd'){
        options.innerHTML = "<a href='/upload?q="+query+ele.getAttribute('key')+"/'><u>V</u>iew</a><hr><label for='dsub'><u>D</u>elete</label>";
    }else{
        options.innerHTML = "<a href='http://"+domain+".unet/"+query+ele.getAttribute('key')+"' target='_blank'><u>V</u>iew</a><hr><label for='cvis'><u>C</u>hange Visibility</label><hr><label for='dsub'><u>D</u>elete</label>";
    }

    ele.style.backgroundColor = '#353738';

    //IF TYPE IS DIR HIDE PRIVATE PUBLIC CHANGER

    changeVisForm.innerHTML = "<input name='type' type='hidden' value='CVIS' required><input name='name' type='text' value='"+ele.getAttribute('key')+"' required min='1' max='20'><input id='cvis' type='submit'>";
    deleteForm.innerHTML = "<input name='type' type='hidden' value='DFILE' required><input name='name' type='text' value='"+ele.getAttribute('key')+"' required min='1' max='20'><input id='dsub' type='submit'>";
    event.preventDefault();
}

function hideOptions(){
    selectedEle.style.backgroundColor = '';
    overlay.style.display = 'none';
    options.style.display = 'none';
}



atoz(document.querySelectorAll('th')[0]);

document.querySelectorAll('th').forEach(th => th.onclick = function(event){
    document.querySelectorAll('th').forEach(th => th.className = '');

    if(selected != th || selected == null){
        atoz(th);

        selected = th;
        th.className = 'd';
    }else{
        ztoa(th);

        selected = null;
        th.className = 'u';
    }
});

function atoz(th){
    var index = Array.from(th.parentNode.children).indexOf(th);
    var tab = Array.from(table.querySelectorAll('tr'));
    while(true){
        var switched = false;
        for(var i = 1; i < tab.length; i++){
            if(tab[i].children[index].textContent.toLocaleLowerCase() < tab[i-1].children[index].textContent.toLocaleLowerCase()){
                table.insertBefore(tab[i], tab[i-1]);
                switched = true;
            }
        }

        if(switched == false){
            break;
        }else{
            tab = Array.from(table.querySelectorAll('tr'));
        }
    }
}

function ztoa(th){
    var index = Array.from(th.parentNode.children).indexOf(th);
    var tab = Array.from(table.querySelectorAll('tr'));

    while(true){
        var switched = false;
        for(var i = 1; i < tab.length; i++){
            if(tab[i].children[index].textContent.toLocaleLowerCase() > tab[i-1].children[index].textContent.toLocaleLowerCase()){
                table.insertBefore(tab[i], tab[i-1]);
                switched = true;
            }
        }

        if(switched == false){
            break;
        }else{
            tab = Array.from(table.querySelectorAll('tr'));
        }
    }
}