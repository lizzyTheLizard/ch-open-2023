// noinspection JSUnresolvedReference,JSUnusedGlobalSymbols

$(function () {
    $('#decadeclose').click(function(){
        const url = new URL(document.URL);
        const params = url.searchParams;
        params.delete('decade')
        window.location.href=url;
    });
    $('#decadeselect').change(function(){
        const url = new URL(document.URL);
        const params = url.searchParams;
        params.set("decade",$('#decadeselect').val())
        window.location.href=url;
    });
    $('#teamclose').click(function(){
        const url = new URL(document.URL);
        const params = url.searchParams;
        params.delete('team')
        window.location.href=url;
    });
    $('#teamselect').change(function(){
        const url = new URL(document.URL);
        const params = url.searchParams;
        params.set("team",$('#teamselect').val())
        window.location.href=url;
    });
    $('#genreclose').click(function(){
        const url = new URL(document.URL);
        const params = url.searchParams;
        params.delete('genre')
        window.location.href=url;
    });
    $('#genreselect').change(function(){
        const url = new URL(document.URL);
        const params = url.searchParams;
        params.set("genre",$('#genreselect').val())
        window.location.href=url;
    });
    $('#searchResults').bootstrapTable({
        onClickRow(item) {
            window.location.href = '/games/' + item[0];
        }
    })
});
