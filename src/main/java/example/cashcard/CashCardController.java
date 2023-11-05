package example.cashcard;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcard")
public class CashCardController {
    private CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    /**
     * Retrieves a CashCard by its ID.
     *
     * @param  requestedId   the ID of the CashCard to retrieve
     * @param  principal     the principal object representing the currently logged in user
     * @return               a ResponseEntity containing the retrieved CashCard if found, or a response with status 404 if not found
     */
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Creates a new cash card.
     *
     * @param  newCashCardRequest  the cash card to be created
     * @param  ucb                 the UriComponentsBuilder for constructing the URI of the new cash card
     * @param  principal           the principal object representing the authenticated user
     * @return                     the ResponseEntity with the created cash card and the URI of the new cash card
     */
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb
                .path("cashcard/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    /**
     * Retrieves all the CashCard objects using pagination and the authenticated user's principal.
     *
     * @param  pageable   the pagination information
     * @param  principal  the authenticated user's principal
     * @return            the ResponseEntity containing a list of CashCard objects
     */
    @GetMapping
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    /**
     * Updates a cash card.
     *
     * @param  requestedId       the ID of the cash card to be updated
     * @param  cashCardUpdate    the updated cash card object
     * @param  principal         the authenticated user
     * @return                   the response entity with no content if the cash card is found and updated,
     *                           otherwise a response entity with 'not found' status
     */
    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal pricipal) {
        CashCard cashCard = findCashCard(requestedId, pricipal);
        if (cashCard != null) {
            CashCard updateCashCard = new CashCard(requestedId, cashCardUpdate.amount(), cashCard.owner());
            cashCardRepository.save(updateCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a cash card by its ID.
     *
     * @param  id          the ID of the cash card to be deleted
     * @param  principal   the principal object representing the authenticated user
     * @return             a ResponseEntity with no content if the cash card was successfully deleted,
     *                     or a ResponseEntity with a "not found" status if the cash card does not exist
     */
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Finds a CashCard by its ID and owner.
     *
     * @param  requestedId   the ID of the CashCard to find
     * @param  principal     the principal object containing the owner's information
     * @return               the CashCard object if found, null otherwise
     */
    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }
}