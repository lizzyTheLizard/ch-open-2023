package site.gutschi.solrexample.connectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.gutschi.solrexample.model.Game;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SolrConnector {

    public Collection<Integer> search(String search) {
        //Not Implemented yet
        return List.of();
    }

    public void reindex(Collection<Game> games) {
        //Not Implemented yet
    }
}
