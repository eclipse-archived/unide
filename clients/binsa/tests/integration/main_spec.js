const pkg  = require('../../package.json'),
      i18n = {
        en: require('../../src/i18n/en.json'),
        de: require('../../src/i18n/de.json')
      };

function deleteDB(prefix = '') {
  return new Promise((res, rej) => {
    let req = indexedDB.deleteDatabase(`_pouch_${prefix}configurations`);
    req.onsuccess = res;
    req.onerror = rej;
  });
}

function resetPage(prefix) {
  Promise.all([
    window.navigator.serviceWorker.getRegistrations()
      .then(registrations =>
        Promise.all(registrations.map(registration =>
          registration.unregister()
        ))
      ),
    window.caches.keys().then(cacheNames =>
      Promise.all(
        cacheNames.map(cacheName => 
          window.caches.delete(cacheName)
        )
      )
    ),
    deleteDB()
  ])
  .then(arg => {
    console.log(prefix);
    if(prefix) {
      return deleteDB(prefix);
    }
    return arg;
  })
  .catch(err => console.error(err));
  cy.visit('http://localhost:8080/binsa/');
  cy.get('.home').should('exist');
  cy.get('#navMenu > div.navbar-end > div.navbar-item.signOut').click({force: true});
}

describe('General', () => {
  it('correct title', () => {
    cy.visit('http://localhost:8080/binsa/')
    cy.title().should('equal', pkg.title);
  });
});

describe('Login', () => {
  before(() => resetPage());

  // clearing service worker
  describe('display', () => {
    it('login modal', () => {
      cy.get('.login.modal.is-active').within(() => {
        cy.get('.modal-background');
        cy.get('.animation-content').within(() => {
          cy.get('header').within(() => {
            cy.get('.modal-card-title').should('contain', i18n.en.login.title);
            cy.get('.dropdown > .dropdown-trigger > i.fa-globe');
          });
          cy.get('.modal-card-body').within(() => {
            cy.get('.field input').not('[disabled]').should('have.length', 2);
            cy.get('.checkbox input').should('have.length', 1);
          });
          cy.get('footer button').should('have.length', 2);
        });
      })
    });
  });

  describe('functionality', () => {

    describe('remember', () => {
      it('should disable credentials', () => {
        cy.get('.login.modal.is-active .animation-content').within(() => {
          cy.get('.modal-card-body .checkbox input').check();
          cy.get('.modal-card-body .field input').should('be.disabled');
          cy.get('footer button.is-primary').should('be.disabled');
        });
      });
      it('should enable credentials again', () => {
        cy.get('.login.modal.is-active .animation-content').within(() => {
          cy.get('.modal-card-body .checkbox input').uncheck();
          cy.get('.modal-card-body .field input').should('not.be.disabled');
          cy.get('footer button.is-primary').should('not.be.disabled');
        });
      });
    });

    describe('i18n', () => {
      before(() => resetPage());

      it('globe should change language', () => {
        cy.get('.login.modal.is-active .animation-content header').within(() => {
          cy.get('.dropdown').as('dropdown').within(() => {
            cy.get('.dropdown-trigger > i.fa-globe').click();
            cy.get('@dropdown');
            cy.should('have.class', 'is-active');
            cy.get('.background');
            cy.get('.dropdown-menu .dropdown-item').contains('de').click({force: true});
          });
          cy.get('.modal-card-title').should('contain', i18n.de.login.title);
        });
      });

      it('and have persisted it', () => {
        cy.get('.login.modal.is-active .animation-content header .modal-card-title').should('contain', i18n.de.login.title);
      });
    });

    describe('general logins', () => {
      before(() => resetPage());

      it('should be possible to continue anonymously', () => {
        cy.get('.login.modal.is-active .animation-content footer button.is-warning').click()
        cy.get('.home').should('exist');
      });
    
      it('should be possible to log out again', () => {
        cy.get('#navMenu > div.navbar-end > div.navbar-item.signOut').click({force: true});
        cy.get('.login.modal');
      });
    });

    describe('named account', () => {
      before(() => resetPage("mickey-"));

      it('should be possible to create a new account', () => {
        cy.get('.login.modal.is-active .animation-content').within(() => {
          cy.get('.modal-card-body').within(() => {
            cy.get('.field:nth-child(1) .input').type("mickey");
            cy.get('.field:nth-child(2) .input').type("house");
          });
          cy.get('footer button.is-primary').click();
        });
        cy.get('.home').should('exist');
      });
    });
  });
});
