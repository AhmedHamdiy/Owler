import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import SearchPage from './Pages/MainPage.js';
import ResultsPage from './Pages/ResultsPage.js';
import NotFound from './Pages/NotFoundPage.js';
function Crawler() {
  return (
    <Router>
      <Switch>
        <Route exact path="/" component={SearchPage} />
        <Route path="/search-results/:qid" component={ResultsPage} />
        <Route path="/not-found/:qid" component={NotFound} />
      </Switch>
    </Router>
  );
}

export default Crawler;
