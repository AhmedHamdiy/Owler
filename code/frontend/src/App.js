import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import SearchPage from './Pages/SearchPage.js';
import ResultsPage from './Pages/ResultsPage.js';
function Owler() {
  return (
    <Router>
      <Switch>
        <Route exact path="/" component={SearchPage} />
        <Route path="/search" component={ResultsPage} />
      </Switch>
    </Router>
  );
}
export default Owler;

