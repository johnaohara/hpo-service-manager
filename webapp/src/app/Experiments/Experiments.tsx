import * as React from 'react';
import {Breadcrumb, BreadcrumbItem, PageSection, Title, Grid, GridItem } from '@patternfly/react-core';
import {ExperimentsTable} from './components/ExperimentsTable'

const Experiments: React.FunctionComponent = () => (
  <PageSection>
      <Grid hasGutter>
          <GridItem span={12}><BreadcrumbBasic/></GridItem>
          <GridItem span={12}><ExperimentsTable/></GridItem>
      </Grid>
  </PageSection>
)

const BreadcrumbBasic: React.FunctionComponent = () => (
    <Breadcrumb>
        <BreadcrumbItem to="#" isActive>experiments</BreadcrumbItem>
    </Breadcrumb>
);


export { Experiments };
